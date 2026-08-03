package com.intentwise.ingestion.application.service;

import com.intentwise.ingestion.application.event.DomainEventPublisher;
import com.intentwise.ingestion.domain.model.IngestionJob;
import com.intentwise.ingestion.domain.model.JobStatus;
import com.intentwise.ingestion.domain.repository.StorageService;
import com.intentwise.ingestion.domain.service.JobStateMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Application service coordinating job status transitions, persistence, and event publishing.
 * Ensures state validations are evaluated through the JobStateMachine before storage.
 */
@Service
@RequiredArgsConstructor
public class JobLifecycleService {

    private final StorageService storageService;
    private final JobStateMachine jobStateMachine;
    private final DomainEventPublisher eventPublisher;

    /**
     * Transitions a job's status, validates via JobStateMachine, persists in StorageService,
     * and publishes the associated domain event.
     *
     * @param job          the IngestionJob to update
     * @param targetStatus the target JobStatus
     * @param domainEvent  the domain event associated with this transition
     * @return the persisted updated IngestionJob
     */
    public IngestionJob transitionAndPublish(IngestionJob job, JobStatus targetStatus, Object domainEvent) {
        IngestionJob updatedJob = jobStateMachine.transitionTo(job, targetStatus);
        IngestionJob savedJob = storageService.saveJob(updatedJob);
        if (domainEvent != null) {
            eventPublisher.publish(domainEvent);
        }
        return savedJob;
    }
}
