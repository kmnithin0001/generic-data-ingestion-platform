package com.intentwise.ingestion.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event published when an ingestion job transitions to the COMPLETED state.
 */
public record JobCompletedEvent(
    UUID jobId,
    UUID sourceId,
    UUID correlationId,
    int totalRecordsFetched,
    int totalPagesFetched,
    LocalDateTime timestamp
) {}
