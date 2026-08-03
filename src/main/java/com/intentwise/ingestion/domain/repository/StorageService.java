package com.intentwise.ingestion.domain.repository;

import com.intentwise.ingestion.domain.model.IngestionJob;
import com.intentwise.ingestion.domain.model.JobFilter;
import com.intentwise.ingestion.domain.model.PageResult;
import com.intentwise.ingestion.domain.model.RawApiResponse;
import com.intentwise.ingestion.domain.model.SourceConfiguration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Storage Service interface defining data access operations for domain models.
 * Completely abstract from JPA, database types, or network protocols.
 */
public interface StorageService {

    /**
     * Saves or updates a source configuration.
     *
     * @param source the SourceConfiguration domain model
     * @return the saved SourceConfiguration
     */
    SourceConfiguration saveSource(SourceConfiguration source);

    /**
     * Finds a source configuration by its unique identifier.
     *
     * @param id the UUID of the configuration
     * @return an Optional containing the configuration if found, or empty
     */
    Optional<SourceConfiguration> findSourceById(UUID id);

    /**
     * Saves or updates an ingestion job's status and tracking metrics.
     *
     * @param job the IngestionJob domain model
     * @return the saved IngestionJob
     */
    IngestionJob saveJob(IngestionJob job);

    /**
     * Finds an ingestion job by its unique identifier.
     *
     * @param id the UUID of the job
     * @return an Optional containing the job if found, or empty
     */
    Optional<IngestionJob> findJobById(UUID id);

    /**
     * Inserts a list of raw API responses as a batch.
     *
     * @param responses the list of RawApiResponse domain models to persist
     */
    void saveRawResponses(List<RawApiResponse> responses);

    // Phase 5 Additions

    /**
     * Finds ingestion jobs based on criteria and pagination.
     *
     * @param filter the search criteria
     * @param page   the page index (0-indexed)
     * @param size   the page size
     * @return a PageResult wrapping the jobs content and metadata
     */
    PageResult<IngestionJob> findJobs(JobFilter filter, int page, int size);

    /**
     * Finds raw API responses belonging to a specific job.
     *
     * @param jobId the UUID of the ingestion job
     * @return the list of RawApiResponse models
     */
    List<RawApiResponse> findRawResponsesByJobId(UUID jobId);

    /**
     * Finds an ingestion job by its optional idempotency key.
     *
     * @param idempotencyKey the idempotency key string
     * @return an Optional containing the job if found, or empty
     */
    Optional<IngestionJob> findJobByIdempotencyKey(String idempotencyKey);
}
