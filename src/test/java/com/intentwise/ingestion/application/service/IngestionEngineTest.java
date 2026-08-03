package com.intentwise.ingestion.application.service;

import com.intentwise.ingestion.domain.event.JobCompletedEvent;
import com.intentwise.ingestion.domain.event.JobFailedEvent;
import com.intentwise.ingestion.domain.event.JobStartedEvent;
import com.intentwise.ingestion.domain.model.AuthenticationType;
import com.intentwise.ingestion.domain.model.HttpMethodType;
import com.intentwise.ingestion.domain.model.IngestionJob;
import com.intentwise.ingestion.domain.model.JobStatus;
import com.intentwise.ingestion.domain.model.PaginationType;
import com.intentwise.ingestion.domain.model.ProcessingResult;
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
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class IngestionEngineTest {

    @Mock
    private StorageService storageService;

    @Mock
    private ConnectorRegistry connectorRegistry;

    @Mock
    private PaginationRegistry paginationRegistry;

    @Mock
    private ResponseProcessor responseProcessor;

    @Mock
    private RetryRegistry retryRegistry;

    @Mock
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Mock
    private JobLifecycleService jobLifecycleService;

    @Mock
    private ProgressCalculator progressCalculator;

    @Mock
    private ApiConnector apiConnector;

    @Mock
    private PaginationStrategy paginationStrategy;

    private MeterRegistry meterRegistry;
    private IngestionEngine engine;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        meterRegistry = new SimpleMeterRegistry();
        
        when(retryRegistry.retry(anyString())).thenReturn(Retry.ofDefaults("test-retry"));
        when(circuitBreakerRegistry.circuitBreaker(anyString())).thenReturn(CircuitBreaker.ofDefaults("test-cb"));
        
        when(jobLifecycleService.transitionAndPublish(any(), any(), any())).thenAnswer(inv -> inv.getArgument(0));
        
        engine = new IngestionEngine(
                storageService,
                connectorRegistry,
                paginationRegistry,
                responseProcessor,
                retryRegistry,
                circuitBreakerRegistry,
                meterRegistry,
                jobLifecycleService,
                progressCalculator
        );
    }

    @Test
    void shouldOrchestrateIngestionPipelineSuccessfully() {
        SourceConfiguration source = SourceConfiguration.builder()
                .id(UUID.randomUUID())
                .url("https://api.example.com/v1/posts")
                .method(HttpMethodType.GET)
                .authType(AuthenticationType.NONE)
                .paginationType(PaginationType.PAGE_NUMBER)
                .paginationConfig(Map.of("pageSize", 10))
                .build();

        when(storageService.saveJob(any(IngestionJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(connectorRegistry.getConnector(anyString())).thenReturn(apiConnector);
        when(paginationRegistry.getStrategy(PaginationType.PAGE_NUMBER)).thenReturn(paginationStrategy);

        // Setup pagination loop behavior: Page 1 triggers execution; Page 2 ends loop.
        when(paginationStrategy.hasNextPage(any(), any()))
                .thenReturn(true)  // Page 1
                .thenReturn(false); // End of pagination

        RequestExecutionResult mockResult = RequestExecutionResult.builder()
                .statusCode(200)
                .responseBody("[{}]")
                .executionTime(Duration.ofMillis(50))
                .responseSizeBytes(120L)
                .build();
        when(apiConnector.fetchPage(any())).thenReturn(mockResult);

        ProcessingResult mockProcessResult = new ProcessingResult(
                List.of(Map.of("id", 1)), null, 1, false, Collections.emptyMap()
        );
        when(responseProcessor.process(any(), any())).thenReturn(mockProcessResult);

        IngestionJob finalJob = engine.ingest(source);

        assertNotNull(finalJob);
        assertEquals(1, finalJob.getTotalRecordsFetched());
        assertEquals(1, finalJob.getTotalPagesFetched());

        // Verify storage calls
        verify(storageService, atLeastOnce()).saveJob(any(IngestionJob.class));
        verify(storageService).saveRawResponses(anyList());

        // Verify domain events published through JobLifecycleService
        verify(jobLifecycleService).transitionAndPublish(any(), eq(JobStatus.RUNNING), any(JobStartedEvent.class));
        verify(jobLifecycleService).transitionAndPublish(any(), eq(JobStatus.COMPLETED), any(JobCompletedEvent.class));

        // Verify micrometer metrics incremented
        assertEquals(1.0, meterRegistry.counter("jobs.started").count());
        assertEquals(1.0, meterRegistry.counter("jobs.completed").count());
        assertEquals(1.0, meterRegistry.counter("pages.processed").count());
        assertEquals(1.0, meterRegistry.counter("records.processed").count());
    }

    @Test
    void shouldHandleFailuresAndMarkJobFailed() {
        SourceConfiguration source = SourceConfiguration.builder()
                .id(UUID.randomUUID())
                .url("https://api.example.com/v1/fail")
                .method(HttpMethodType.GET)
                .authType(AuthenticationType.NONE)
                .paginationType(PaginationType.NONE)
                .build();

        when(storageService.saveJob(any(IngestionJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(connectorRegistry.getConnector(anyString())).thenReturn(apiConnector);
        when(paginationRegistry.getStrategy(PaginationType.NONE)).thenReturn(paginationStrategy);
        
        when(paginationStrategy.hasNextPage(any(), any())).thenReturn(true);
        when(apiConnector.fetchPage(any())).thenThrow(new IllegalStateException("Network failure"));

        IngestionJob finalJob = engine.ingest(source);

        assertNotNull(finalJob);
        assertEquals("Network failure", finalJob.getErrorMessage());

        verify(jobLifecycleService).transitionAndPublish(any(), eq(JobStatus.FAILED), any(JobFailedEvent.class));
        assertEquals(1.0, meterRegistry.counter("jobs.failed").count());
    }
}
