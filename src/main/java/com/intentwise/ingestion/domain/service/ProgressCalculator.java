package com.intentwise.ingestion.domain.service;

import java.time.LocalDateTime;

/**
 * Domain service interface responsible for computing percentage completion
 * and estimating execution completion times for active ingestion jobs.
 */
public interface ProgressCalculator {

    /**
     * Calculates completion percentage.
     *
     * @param recordsProcessed the number of records processed so far
     * @param totalRecords     the total number of records expected
     * @return the percentage value between 0.0 and 100.0
     */
    double calculatePercentage(int recordsProcessed, Integer totalRecords);

    /**
     * Calculates estimated completion time based on run rate.
     *
     * @param startTime        the time job execution started
     * @param recordsProcessed the number of records processed so far
     * @param totalRecords     the total number of records expected
     * @param now              the current time reference
     * @return the estimated LocalDateTime of completion, or null if cannot be determined
     */
    LocalDateTime calculateEstimatedCompletion(
            LocalDateTime startTime,
            int recordsProcessed,
            Integer totalRecords,
            LocalDateTime now
    );
}
