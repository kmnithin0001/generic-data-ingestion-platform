package com.intentwise.ingestion.presentation.dto;

import com.intentwise.ingestion.domain.model.IngestionJob;
import com.intentwise.ingestion.domain.model.JobStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable response representing an ingestion job's status and metrics.
 */
public record JobResponse(
    UUID id,
    UUID sourceId,
    JobStatus status,
    LocalDateTime startTime,
    LocalDateTime endTime,
    int pagesProcessed,
    int recordsProcessed,
    Integer totalRecords,
    double percentageCompleted,
    LocalDateTime estimatedCompletion,
    String errorMessage,
    String idempotencyKey,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static JobResponse fromDomain(IngestionJob job) {
        if (job == null) return null;
        return new JobResponse(
            job.getId(),
            job.getSourceId(),
            job.getStatus(),
            job.getStartTime(),
            job.getEndTime(),
            job.getTotalPagesFetched() != null ? job.getTotalPagesFetched() : 0,
            job.getTotalRecordsFetched() != null ? job.getTotalRecordsFetched() : 0,
            job.getTotalRecords(),
            job.getPercentageCompleted(),
            job.getEstimatedCompletion(),
            job.getErrorMessage(),
            job.getIdempotencyKey(),
            job.getCreatedAt(),
            job.getUpdatedAt()
        );
    }
}
