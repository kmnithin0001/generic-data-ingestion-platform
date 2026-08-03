package com.intentwise.ingestion.domain.service;

import com.intentwise.ingestion.domain.exception.InvalidJobStateTransitionException;
import com.intentwise.ingestion.domain.exception.JobAlreadyCancelledException;
import com.intentwise.ingestion.domain.exception.JobAlreadyCompletedException;
import com.intentwise.ingestion.domain.model.IngestionJob;
import com.intentwise.ingestion.domain.model.JobStatus;

import java.time.LocalDateTime;

/**
 * Default implementation of JobStateMachine.
 * Validates transition rules and raises meaningful DomainExceptions for invalid transitions.
 */
public class DefaultJobStateMachine implements JobStateMachine {

    @Override
    public IngestionJob transitionTo(IngestionJob job, JobStatus targetStatus) {
        JobStatus current = job.getStatus();
        if (current == targetStatus) {
            return job;
        }

        // Validate terminal status exceptions
        if (current == JobStatus.CANCELLED) {
            throw new JobAlreadyCancelledException("Job " + job.getId() + " is already cancelled");
        }
        if (current == JobStatus.COMPLETED) {
            throw new JobAlreadyCompletedException("Job " + job.getId() + " is already completed");
        }

        boolean valid = switch (current) {
            case PENDING -> targetStatus == JobStatus.RUNNING || targetStatus == JobStatus.CANCELLED;
            case RUNNING -> targetStatus == JobStatus.COMPLETED || targetStatus == JobStatus.FAILED || targetStatus == JobStatus.CANCELLED;
            default -> false;
        };

        if (!valid) {
            throw new InvalidJobStateTransitionException("Invalid transition from " + current + " to " + targetStatus);
        }

        return job.toBuilder()
                .status(targetStatus)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
