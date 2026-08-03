package com.intentwise.ingestion.application.usecase;

import com.intentwise.ingestion.domain.model.IngestionJob;
import com.intentwise.ingestion.domain.model.JobFilter;
import com.intentwise.ingestion.domain.model.PageResult;
import com.intentwise.ingestion.domain.repository.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Use case responsible for listing ingestion jobs matching filters and paginated boundaries.
 */
@Component
@RequiredArgsConstructor
public class ListJobsUseCase {

    private final StorageService storageService;

    /**
     * Retrieves a page of filtered ingestion jobs.
     *
     * @param filter criteria for filtering jobs
     * @param page   page number (0-indexed)
     * @param size   page size
     * @return PageResult wrapping the matched jobs
     */
    public PageResult<IngestionJob> execute(JobFilter filter, int page, int size) {
        return storageService.findJobs(filter, page, size);
    }
}
