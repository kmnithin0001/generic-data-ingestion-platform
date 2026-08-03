package com.intentwise.ingestion.infrastructure.persistence.entity;

import com.intentwise.ingestion.domain.model.JobStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity representing the `ingestion_jobs` table.
 * Tracks performance, progress calculations, status metrics, and idempotency mappings.
 */
@Entity
@Table(
    name = "ingestion_jobs",
    indexes = {
        @Index(name = "idx_job_status", columnList = "status"),
        @Index(name = "idx_job_source_id", columnList = "source_id"),
        @Index(name = "idx_job_idempotency_key", columnList = "idempotency_key")
    }
)
@Getter
@Setter
public class IngestionJobEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "source_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_job_source")
    )
    private SourceConfigurationEntity source;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private JobStatus status;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "total_records_fetched")
    private Integer totalRecordsFetched;

    @Column(name = "total_pages_fetched")
    private Integer totalPagesFetched;

    @Column(name = "error_message", length = 2048)
    private String errorMessage;

    // Phase 5 additions
    @Column(name = "idempotency_key", length = 255)
    private String idempotencyKey;

    @Column(name = "total_records")
    private Integer totalRecords;

    @Column(name = "percentage_completed")
    private double percentageCompleted;

    @Column(name = "estimated_completion")
    private LocalDateTime estimatedCompletion;
}
