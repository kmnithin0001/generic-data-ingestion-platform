package com.intentwise.ingestion.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event published when an ingestion job transitions to the FAILED state.
 */
public record JobFailedEvent(
    UUID jobId,
    UUID sourceId,
    UUID correlationId,
    String errorMessage,
    LocalDateTime timestamp
) {}
