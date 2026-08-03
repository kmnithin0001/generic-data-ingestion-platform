package com.intentwise.ingestion.application.usecase;

import com.intentwise.ingestion.domain.exception.DomainException;
import com.intentwise.ingestion.domain.model.RawApiResponse;
import com.intentwise.ingestion.domain.repository.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Use case responsible for retrieving raw API response payloads collected for a job.
 */
@Component
@RequiredArgsConstructor
public class GetRawResponsesUseCase {

    private final StorageService storageService;

    /**
     * Retrieves all raw API responses associated with a job.
     * Throws DomainException if the job does not exist.
     *
     * @param jobId the UUID of the ingestion job
     * @return the list of RawApiResponse domain models
     */
    public List<RawApiResponse> execute(UUID jobId) {
        // Verify job existence first
        storageService.findJobById(jobId)
                .orElseThrow(() -> new DomainException("Job not found: " + jobId));

        return storageService.findRawResponsesByJobId(jobId);
    }
}
