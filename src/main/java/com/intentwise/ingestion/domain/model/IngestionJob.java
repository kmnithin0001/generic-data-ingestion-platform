package com.intentwise.ingestion.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Pure domain model representing an execution run of an ingestion pipeline.
 * Tracks run status, timing metrics, failure reasons, item volume metrics,
 * progress percentages, and idempotency mapping keys.
 */
@Value
@Builder(toBuilder = true)
public class IngestionJob {
    UUID id;
    UUID sourceId;
    JobStatus status;
    LocalDateTime startTime;
    LocalDateTime endTime;
    Integer totalRecordsFetched;
    Integer totalPagesFetched;
    String errorMessage;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    // Phase 5 Fields
    String idempotencyKey;
    Integer totalRecords;
    double percentageCompleted;
    LocalDateTime estimatedCompletion;
}
