package com.intentwise.ingestion.domain.service;

import com.intentwise.ingestion.domain.exception.InvalidJobStateTransitionException;
import com.intentwise.ingestion.domain.exception.JobAlreadyCancelledException;
import com.intentwise.ingestion.domain.exception.JobAlreadyCompletedException;
import com.intentwise.ingestion.domain.model.IngestionJob;
import com.intentwise.ingestion.domain.model.JobStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JobStateMachineTest {

    private final JobStateMachine stateMachine = new DefaultJobStateMachine();

    @Test
    void shouldTransitionPendingToRunning() {
        IngestionJob job = createJob(JobStatus.PENDING);
        IngestionJob updated = stateMachine.transitionTo(job, JobStatus.RUNNING);
        assertEquals(JobStatus.RUNNING, updated.getStatus());
    }

    @Test
    void shouldTransitionPendingToCancelled() {
        IngestionJob job = createJob(JobStatus.PENDING);
        IngestionJob updated = stateMachine.transitionTo(job, JobStatus.CANCELLED);
        assertEquals(JobStatus.CANCELLED, updated.getStatus());
    }

    @Test
    void shouldTransitionRunningToCompleted() {
        IngestionJob job = createJob(JobStatus.RUNNING);
        IngestionJob updated = stateMachine.transitionTo(job, JobStatus.COMPLETED);
        assertEquals(JobStatus.COMPLETED, updated.getStatus());
    }

    @Test
    void shouldTransitionRunningToFailed() {
        IngestionJob job = createJob(JobStatus.RUNNING);
        IngestionJob updated = stateMachine.transitionTo(job, JobStatus.FAILED);
        assertEquals(JobStatus.FAILED, updated.getStatus());
    }

    @Test
    void shouldTransitionRunningToCancelled() {
        IngestionJob job = createJob(JobStatus.RUNNING);
        IngestionJob updated = stateMachine.transitionTo(job, JobStatus.CANCELLED);
        assertEquals(JobStatus.CANCELLED, updated.getStatus());
    }

    @Test
    void shouldNotTransitionPendingToCompleted() {
        IngestionJob job = createJob(JobStatus.PENDING);
        assertThrows(InvalidJobStateTransitionException.class, () -> 
                stateMachine.transitionTo(job, JobStatus.COMPLETED)
        );
    }

    @Test
    void shouldNotTransitionCompletedToRunning() {
        IngestionJob job = createJob(JobStatus.COMPLETED);
        assertThrows(JobAlreadyCompletedException.class, () -> 
                stateMachine.transitionTo(job, JobStatus.RUNNING)
        );
    }

    @Test
    void shouldNotTransitionCancelledToRunning() {
        IngestionJob job = createJob(JobStatus.CANCELLED);
        assertThrows(JobAlreadyCancelledException.class, () -> 
                stateMachine.transitionTo(job, JobStatus.RUNNING)
        );
    }

    private IngestionJob createJob(JobStatus status) {
        return IngestionJob.builder()
                .id(UUID.randomUUID())
                .sourceId(UUID.randomUUID())
                .status(status)
                .build();
    }
}
