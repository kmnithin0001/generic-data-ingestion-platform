package com.intentwise.ingestion.domain.service;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Default implementation of ProgressCalculator.
 * Computes ingestion progress metrics without side effects or dependencies.
 */
public class DefaultProgressCalculator implements ProgressCalculator {

    @Override
    public double calculatePercentage(int recordsProcessed, Integer totalRecords) {
        if (totalRecords == null || totalRecords <= 0) {
            return 0.0;
        }
        double pct = (recordsProcessed * 100.0) / totalRecords;
        return Math.min(100.0, Math.max(0.0, pct));
    }

    @Override
    public LocalDateTime calculateEstimatedCompletion(
            LocalDateTime startTime,
            int recordsProcessed,
            Integer totalRecords,
            LocalDateTime now
    ) {
        if (totalRecords == null || totalRecords <= 0 || recordsProcessed <= 0 || startTime == null || now == null) {
            return null;
        }
        if (recordsProcessed >= totalRecords) {
            return now;
        }
        long elapsedNs = Duration.between(startTime, now).toNanos();
        if (elapsedNs <= 0) {
            return null;
        }
        double nsPerRecord = (double) elapsedNs / recordsProcessed;
        long remainingRecords = totalRecords - recordsProcessed;
        long remainingNs = (long) (nsPerRecord * remainingRecords);
        return now.plusNanos(remainingNs);
    }
}
