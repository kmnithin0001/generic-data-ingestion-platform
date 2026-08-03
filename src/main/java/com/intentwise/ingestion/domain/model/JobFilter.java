package com.intentwise.ingestion.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Filter criteria for querying ingestion jobs in a decoupled manner.
 */
public record JobFilter(
    JobStatus status,
    UUID sourceId,
    LocalDateTime createdAfter,
    LocalDateTime createdBefore
) {}
