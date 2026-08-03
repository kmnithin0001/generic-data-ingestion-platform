package com.intentwise.ingestion.application.service;

import com.intentwise.ingestion.domain.event.JobCompletedEvent;
import com.intentwise.ingestion.domain.event.JobFailedEvent;
import com.intentwise.ingestion.domain.event.JobStartedEvent;
import com.intentwise.ingestion.domain.event.JobCancelledEvent;
import com.intentwise.ingestion.domain.exception.JobCancelledException;
import com.intentwise.ingestion.domain.model.IngestionContext;
import com.intentwise.ingestion.domain.model.IngestionJob;
import com.intentwise.ingestion.domain.model.JobStatus;
import com.intentwise.ingestion.domain.model.ProcessingResult;
import com.intentwise.ingestion.domain.model.RawApiResponse;
import com.intentwise.ingestion.domain.model.RequestExecutionResult;
import com.intentwise.ingestion.domain.model.SourceConfiguration;
import com.intentwise.ingestion.domain.repository.StorageService;
import com.intentwise.ingestion.domain.service.ApiConnector;
import com.intentwise.ingestion.domain.service.ResponseProcessor;
import com.intentwise.ingestion.domain.service.ProgressCalculator;
import com.intentwise.ingestion.domain.service.factory.ConnectorRegistry;
import com.intentwise.ingestion.domain.service.factory.PaginationRegistry;
import com.intentwise.ingestion.domain.service.strategy.PaginationStrategy;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Core orchestration engine for generic data ingestion.
 * Runs the pipeline stages: Validation, Strategy Resolution, Request loops,
 * Response processing, Batch persistence, and Observability tracking.
 * Coordinates status updates and event publishing via JobLifecycleService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionEngine {

    private final StorageService storageService;
    private final ConnectorRegistry connectorRegistry;
    private final PaginationRegistry paginationRegistry;
    private final ResponseProcessor responseProcessor;
    
    private final RetryRegistry retryRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final MeterRegistry meterRegistry;
    
    private final JobLifecycleService jobLifecycleService;
    private final ProgressCalculator progressCalculator;

    @Value("${app.ingestion.progress-interval:1}")
    private int progressInterval = 1;

    /**
     * Overloaded entry point to trigger ingestion for a given source configuration (generates a random job ID).
     *
     * @param sourceConfig the configuration describing how to ingest data.
     * @return the finalized IngestionJob metrics tracking
     */
    public IngestionJob ingest(SourceConfiguration sourceConfig) {
        return ingest(sourceConfig, UUID.randomUUID());
    }

    /**
     * Entry point to trigger ingestion for a given source configuration with a specific job ID.
     *
     * @param sourceConfig the configuration describing how to ingest data.
     * @param jobId        the pre-generated UUID of the job
     * @return the finalized IngestionJob metrics tracking
     */
    public IngestionJob ingest(SourceConfiguration sourceConfig, UUID jobId) {
        // 1. Validate configuration
        validateSourceConfig(sourceConfig);

        UUID correlationId = UUID.randomUUID();

        // 2. Resolve job state (creates a pending job if it does not exist)
        IngestionJob job = storageService.findJobById(jobId).orElseGet(() -> {
            IngestionJob pending = IngestionJob.builder()
                    .id(jobId)
                    .sourceId(sourceConfig.getId())
                    .status(JobStatus.PENDING)
                    .startTime(LocalDateTime.now())
                    .totalRecordsFetched(0)
                    .totalPagesFetched(0)
                    .build();
            return storageService.saveJob(pending);
        });

        // 3. Transition to RUNNING state and publish event
        job = jobLifecycleService.transitionAndPublish(job, JobStatus.RUNNING,
                new JobStartedEvent(job.getId(), sourceConfig.getId(), correlationId, LocalDateTime.now()));
        meterRegistry.counter("jobs.started").increment();

        // Build ingestion context
        IngestionContext context = IngestionContext.builder()
                .correlationId(correlationId)
                .sourceConfig(sourceConfig)
                .job(job)
                .currentPageNumber(0)
                .totalRecordsFetched(0)
                .totalBytesFetched(0)
                .retryCount(0)
                .build();

        // Resolve strategies
        ApiConnector connector = connectorRegistry.getConnector("REST");
        PaginationStrategy paginationStrategy = paginationRegistry.getStrategy(sourceConfig.getPaginationType());

        // Setup Resilience4j handlers
        Retry retry = retryRegistry.retry("ingestionEngine");
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("ingestionEngine");
        
        retry.getEventPublisher().onRetry(event -> {
            context.incrementRetryCount();
            meterRegistry.counter("ingestion.retries.total").increment();
            log.warn("Transient connection retry #{} occurred: {}", context.getRetryCount(), event.getLastThrowable().getMessage());
        });

        Timer.Sample timerSample = Timer.start(meterRegistry);

        // Set structured logging context
        setupMdc(context);

        ProcessingResult lastResult = null;
        try {
            log.info("Starting ingestion run. Base URL: {}", sourceConfig.getUrl());

            while (paginationStrategy.hasNextPage(context, lastResult)) {
                // Check user cancellation before requesting next page
                if (isCancelled(jobId)) {
                    throw new JobCancelledException("Job was cancelled by user request");
                }

                int nextPageNum = context.getCurrentPageNumber() + 1;
                log.info("Processing Ingestion page #{}", nextPageNum);

                // Setup header & query options for next page
                Map<String, List<String>> headers = new HashMap<>();
                Map<String, List<String>> queryParams = new HashMap<>();
                paginationStrategy.setupNextPage(context, lastResult, headers, queryParams);

                // Run request with retry/circuit-breaker policies
                RequestExecutionResult executionResult = executeWithResilience(connector, context, retry, circuitBreaker);

                // Process response body
                lastResult = responseProcessor.process(executionResult, sourceConfig.getPaginationConfig());

                // Build and buffer raw response
                RawApiResponse rawResponse = RawApiResponse.builder()
                        .id(UUID.randomUUID())
                        .jobId(job.getId())
                        .pageNumber(nextPageNum)
                        .requestUrl(executionResult.getRequestUrl())
                        .requestHeaders(convertToFlatMap(headers))
                        .responseBody(executionResult.getResponseBody())
                        .responseMetadata(Map.of(
                                "statusCode", executionResult.getStatusCode(),
                                "latencyMs", executionResult.getExecutionTime().toMillis()
                        ))
                        .build();

                context.bufferResponse(rawResponse);

                // Update context metrics
                context.setCurrentPageNumber(nextPageNum);
                context.setTotalRecordsFetched(context.getTotalRecordsFetched() + lastResult.records().size());
                context.setTotalBytesFetched(context.getTotalBytesFetched() + (int) executionResult.getResponseSizeBytes());

                // Increment instrumentation counters
                meterRegistry.counter("pages.processed").increment();
                meterRegistry.counter("records.processed").increment(lastResult.records().size());

                // Calculate progress percentages using ProgressCalculator
                Integer totalExpectedRecords = lastResult.totalRecords();
                double progressPct = progressCalculator.calculatePercentage(context.getTotalRecordsFetched(), totalExpectedRecords);
                LocalDateTime estComp = progressCalculator.calculateEstimatedCompletion(
                        job.getStartTime(), context.getTotalRecordsFetched(), totalExpectedRecords, LocalDateTime.now());

                job = job.toBuilder()
                        .totalRecordsFetched(context.getTotalRecordsFetched())
                        .totalPagesFetched(context.getCurrentPageNumber())
                        .totalRecords(totalExpectedRecords)
                        .percentageCompleted(progressPct)
                        .estimatedCompletion(estComp)
                        .updatedAt(LocalDateTime.now())
                        .build();

                // Configurable progress persistence: save only every N pages
                if (nextPageNum % progressInterval == 0 || !paginationStrategy.hasNextPage(context, lastResult)) {
                    job = storageService.saveJob(job);
                }

                // Batch persist raw responses (every 10 pages, or immediately if next URL is not expected)
                if (context.getBufferedResponses().size() >= 10 || !paginationStrategy.hasNextPage(context, lastResult)) {
                    log.info("Flushing raw response batch (size: {}) to storage", context.getBufferedResponses().size());
                    storageService.saveRawResponses(context.getBufferedResponses());
                    context.clearResponseBuffer();
                }
            }

            // Ingestion run completed successfully
            log.info("Ingestion run completed successfully. Total records fetched: {}, Total pages: {}", 
                    context.getTotalRecordsFetched(), context.getCurrentPageNumber());

            job = job.toBuilder()
                    .endTime(LocalDateTime.now())
                    .build();

            job = jobLifecycleService.transitionAndPublish(job, JobStatus.COMPLETED,
                    new JobCompletedEvent(job.getId(), sourceConfig.getId(), correlationId, 
                            job.getTotalRecordsFetched(), job.getTotalPagesFetched(), LocalDateTime.now()));
            meterRegistry.counter("jobs.completed").increment();

        } catch (JobCancelledException e) {
            log.warn("Ingestion job was cancelled: {}", e.getMessage());

            job = job.toBuilder()
                    .endTime(LocalDateTime.now())
                    .errorMessage(e.getMessage())
                    .build();

            job = jobLifecycleService.transitionAndPublish(job, JobStatus.CANCELLED,
                    new JobCancelledEvent(job.getId(), sourceConfig.getId(), correlationId, LocalDateTime.now()));
            meterRegistry.counter("jobs.cancelled").increment();

        } catch (Exception e) {
            log.error("Ingestion job failed. Error details: {}", e.getMessage(), e);

            job = job.toBuilder()
                    .endTime(LocalDateTime.now())
                    .errorMessage(e.getMessage())
                    .build();

            job = jobLifecycleService.transitionAndPublish(job, JobStatus.FAILED,
                    new JobFailedEvent(job.getId(), sourceConfig.getId(), correlationId, e.getMessage(), LocalDateTime.now()));
            meterRegistry.counter("jobs.failed").increment();

        } finally {
            timerSample.stop(meterRegistry.timer("average.execution.time"));
            MDC.clear();
        }

        return job;
    }

    private boolean isCancelled(UUID jobId) {
        return storageService.findJobById(jobId)
                .map(job -> job.getStatus() == JobStatus.CANCELLED)
                .orElse(false);
    }

    private RequestExecutionResult executeWithResilience(ApiConnector connector, IngestionContext context, Retry retry, CircuitBreaker cb) {
        return cb.executeSupplier(
            Retry.decorateSupplier(retry, () -> {
                RequestExecutionResult result = connector.fetchPage(context);
                if (result.getStatusCode() >= 400) {
                    throw new IllegalStateException("HTTP execution returned error status: " + result.getStatusCode() 
                            + " for URL: " + result.getRequestUrl());
                }
                return result;
            })
        );
    }

    private void validateSourceConfig(SourceConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("Source configuration must not be null");
        }
        if (config.getUrl() == null || (!config.getUrl().startsWith("http://") && !config.getUrl().startsWith("https://"))) {
            throw new IllegalArgumentException("Invalid base URL configuration");
        }
        if (config.getMethod() == null) {
            throw new IllegalArgumentException("HTTP Method must not be null");
        }
        if (config.getAuthType() == null) {
            throw new IllegalArgumentException("Authentication Type must not be null");
        }
        if (config.getPaginationType() == null) {
            throw new IllegalArgumentException("Pagination Type must not be null");
        }
    }

    private void setupMdc(IngestionContext context) {
        MDC.clear();
        MDC.put("correlationId", context.getCorrelationId().toString());
        MDC.put("jobId", context.getJob().getId().toString());
        MDC.put("sourceId", context.getSourceConfig().getId().toString());
    }

    private Map<String, Object> convertToFlatMap(Map<String, List<String>> originalMap) {
        if (originalMap == null) return Collections.emptyMap();
        Map<String, Object> flatMap = new HashMap<>();
        originalMap.forEach((k, v) -> {
            if (v != null && !v.isEmpty()) {
                flatMap.put(k, v.size() == 1 ? v.getFirst() : v);
            }
        });
        return flatMap;
    }
}
