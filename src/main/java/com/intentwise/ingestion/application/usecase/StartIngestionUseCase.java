package com.intentwise.ingestion.application.usecase;

import com.intentwise.ingestion.application.event.DomainEventPublisher;
import com.intentwise.ingestion.application.service.IngestionEngine;
import com.intentwise.ingestion.domain.event.JobCreatedEvent;
import com.intentwise.ingestion.domain.model.IngestionJob;
import com.intentwise.ingestion.domain.model.JobStatus;
import com.intentwise.ingestion.domain.model.SourceConfiguration;
import com.intentwise.ingestion.domain.repository.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Use case responsible for launching a new data ingestion workflow.
 * Implements strict idempotency key checks and schedules execution asynchronously.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartIngestionUseCase {

    private final StorageService storageService;
    private final IngestionEngine ingestionEngine;
    private final DomainEventPublisher eventPublisher;
    private final AsyncTaskExecutor ingestionTaskExecutor;

    /**
     * Triggers the ingestion pipeline.
     *
     * @param sourceConfig   the configuration describing how to ingest data
     * @param idempotencyKey optional idempotency key
     * @param correlationId  correlation ID for request tracking
     * @return the created or already existing job
     */
    public IngestionJob execute(SourceConfiguration sourceConfig, String idempotencyKey, UUID correlationId) {
        UUID finalCorrId = correlationId != null ? correlationId : UUID.randomUUID();

        // 1. Check idempotency key if provided
        if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
            Optional<IngestionJob> existingJob = storageService.findJobByIdempotencyKey(idempotencyKey);
            if (existingJob.isPresent()) {
                log.info("Idempotency key match found for key: {}. Returning existing job: {}", 
                        idempotencyKey, existingJob.get().getId());
                return existingJob.get();
            }
        }

        // 2. Persist SourceConfiguration first (generate ID if null)
        SourceConfiguration savedSource = sourceConfig;
        if (sourceConfig.getId() == null) {
            savedSource = sourceConfig.toBuilder().id(UUID.randomUUID()).build();
        }
        savedSource = storageService.saveSource(savedSource);

        // 3. Create a PENDING job
        UUID jobId = UUID.randomUUID();
        IngestionJob pendingJob = IngestionJob.builder()
                .id(jobId)
                .sourceId(savedSource.getId())
                .status(JobStatus.PENDING)
                .startTime(LocalDateTime.now())
                .totalRecordsFetched(0)
                .totalPagesFetched(0)
                .percentageCompleted(0.0)
                .idempotencyKey(idempotencyKey)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        IngestionJob savedJob = storageService.saveJob(pendingJob);

        // 4. Publish JobCreatedEvent
        eventPublisher.publish(new JobCreatedEvent(savedJob.getId(), savedSource.getId(), finalCorrId, LocalDateTime.now()));

        // 5. Trigger IngestionEngine asynchronously
        final SourceConfiguration finalSource = savedSource;
        ingestionTaskExecutor.execute(() -> {
            try {
                ingestionEngine.ingest(finalSource, jobId);
            } catch (Exception e) {
                log.error("Asynchronous ingestion execution failed for job: {}", jobId, e);
            }
        });

        return savedJob;
    }
}
