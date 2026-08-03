package com.intentwise.ingestion.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event published when an ingestion job transitions to the RUNNING state.
 */
public record JobStartedEvent(
    UUID jobId,
    UUID sourceId,
    UUID correlationId,
    LocalDateTime timestamp
) {}
