package com.intentwise.ingestion.application.usecase;

import com.intentwise.ingestion.application.event.DomainEventPublisher;
import com.intentwise.ingestion.application.service.IngestionEngine;
import com.intentwise.ingestion.domain.event.JobCreatedEvent;
import com.intentwise.ingestion.domain.exception.JobAlreadyCompletedException;
import com.intentwise.ingestion.domain.exception.DomainException;
import com.intentwise.ingestion.domain.model.IngestionJob;
import com.intentwise.ingestion.domain.model.JobStatus;
import com.intentwise.ingestion.domain.model.SourceConfiguration;
import com.intentwise.ingestion.domain.repository.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Use case responsible for retrying a previously failed or cancelled ingestion job.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RetryIngestionUseCase {

    private final StorageService storageService;
    private final IngestionEngine ingestionEngine;
    private final DomainEventPublisher eventPublisher;
    private final AsyncTaskExecutor ingestionTaskExecutor;

    /**
     * Executes the retry workflow.
     *
     * @param failedJobId   the UUID of the failed ingestion job
     * @param correlationId correlation ID for request tracking
     * @return the new ingestion job scheduled for retry
     */
    public IngestionJob execute(UUID failedJobId, UUID correlationId) {
        UUID finalCorrId = correlationId != null ? correlationId : UUID.randomUUID();

        // 1. Fetch original job
        IngestionJob failedJob = storageService.findJobById(failedJobId)
                .orElseThrow(() -> new DomainException("Job not found: " + failedJobId));

        // 2. Validate status for retry eligibility
        if (failedJob.getStatus() == JobStatus.RUNNING || failedJob.getStatus() == JobStatus.PENDING) {
            throw new DomainException("Cannot retry active job: " + failedJobId);
        }
        if (failedJob.getStatus() == JobStatus.COMPLETED) {
            throw new JobAlreadyCompletedException("Cannot retry successfully completed job: " + failedJobId);
        }

        // 3. Resolve original configuration
        SourceConfiguration sourceConfig = storageService.findSourceById(failedJob.getSourceId())
                .orElseThrow(() -> new DomainException("Source configuration not found for job: " + failedJobId));

        // 4. Create new retry job
        UUID retryJobId = UUID.randomUUID();
        IngestionJob retryJob = IngestionJob.builder()
                .id(retryJobId)
                .sourceId(sourceConfig.getId())
                .status(JobStatus.PENDING)
                .startTime(LocalDateTime.now())
                .totalRecordsFetched(0)
                .totalPagesFetched(0)
                .percentageCompleted(0.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        IngestionJob savedJob = storageService.saveJob(retryJob);

        // 5. Publish JobCreatedEvent
        eventPublisher.publish(new JobCreatedEvent(savedJob.getId(), sourceConfig.getId(), finalCorrId, LocalDateTime.now()));

        // 6. Trigger async execution
        ingestionTaskExecutor.execute(() -> {
            try {
                ingestionEngine.ingest(sourceConfig, retryJobId);
            } catch (Exception e) {
                log.error("Asynchronous retry execution failed for job: {}", retryJobId, e);
            }
        });

        return savedJob;
    }
}
