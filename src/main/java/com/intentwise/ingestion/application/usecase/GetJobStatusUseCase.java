package com.intentwise.ingestion.application.usecase;

import com.intentwise.ingestion.domain.exception.DomainException;
import com.intentwise.ingestion.domain.model.IngestionJob;
import com.intentwise.ingestion.domain.repository.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Use case responsible for retrieving details and statistics for a specific ingestion job.
 */
@Component
@RequiredArgsConstructor
public class GetJobStatusUseCase {

    private final StorageService storageService;

    /**
     * Retrieves the ingestion job status.
     *
     * @param jobId the UUID of the job
     * @return the IngestionJob domain model
     */
    public IngestionJob execute(UUID jobId) {
        return storageService.findJobById(jobId)
                .orElseThrow(() -> new DomainException("Job not found: " + jobId));
    }
}
