package com.intentwise.ingestion.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event published when an active ingestion job is cancelled.
 */
public record JobCancelledEvent(
    UUID jobId,
    UUID sourceId,
    UUID correlationId,
    LocalDateTime timestamp
) {}
