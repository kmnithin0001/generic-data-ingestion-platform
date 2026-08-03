package com.intentwise.ingestion.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event published when an ingestion job is first registered in the system.
 */
public record JobCreatedEvent(
    UUID jobId,
    UUID sourceId,
    UUID correlationId,
    LocalDateTime timestamp
) {}
