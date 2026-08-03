package com.intentwise.ingestion.domain.service;

import com.intentwise.ingestion.domain.model.IngestionJob;
import com.intentwise.ingestion.domain.model.JobStatus;

/**
 * Domain service interface defining operations for validating and performing status transitions
 * on ingestion jobs, serving as the single source of truth for job lifecycle states.
 */
public interface JobStateMachine {

    /**
     * Transitions a job to a target state, validating that the transition is legal.
     * Throws InvalidJobStateTransitionException if transition is illegal.
     *
     * @param job          the IngestionJob to transition
     * @param targetStatus the target status to transition to
     * @return the updated IngestionJob with the new status
     */
    IngestionJob transitionTo(IngestionJob job, JobStatus targetStatus);
}
