package com.intentwise.ingestion.application.usecase;

import com.intentwise.ingestion.application.service.JobLifecycleService;
import com.intentwise.ingestion.domain.event.JobCancelledEvent;
import com.intentwise.ingestion.domain.exception.DomainException;
import com.intentwise.ingestion.domain.model.IngestionJob;
import com.intentwise.ingestion.domain.model.JobStatus;
import com.intentwise.ingestion.domain.repository.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Use case responsible for cancelling an active (PENDING or RUNNING) ingestion job.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CancelJobUseCase {

    private final StorageService storageService;
    private final JobLifecycleService jobLifecycleService;

    /**
     * Executes the cancellation request.
     *
     * @param jobId         the UUID of the job to cancel
     * @param correlationId correlation ID for request tracking
     * @return the updated ingestion job record
     */
    public IngestionJob execute(UUID jobId, UUID correlationId) {
        UUID finalCorrId = correlationId != null ? correlationId : UUID.randomUUID();

        // 1. Fetch job
        IngestionJob job = storageService.findJobById(jobId)
                .orElseThrow(() -> new DomainException("Job not found: " + jobId));

        log.info("Requesting cancellation for job: {}", jobId);

        // 2. Transition and publish event (State Machine will validate and throw correct domain exception if terminal)
        return jobLifecycleService.transitionAndPublish(job, JobStatus.CANCELLED,
                new JobCancelledEvent(job.getId(), job.getSourceId(), finalCorrId, LocalDateTime.now()));
    }
}
