package com.intentwise.ingestion.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

/**
 * JPA entity representing the `raw_api_responses` table.
 * Persists exact copies of the received web responses as raw JSON,
 * referencing the parent execution job.
 */
@Entity
@Table(
    name = "raw_api_responses",
    indexes = {
        @Index(name = "idx_response_job_id", columnList = "job_id")
    }
)
@Getter
@Setter
public class RawApiResponseEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "job_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_response_job")
    )
    private IngestionJobEntity job;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    @Column(name = "request_url", nullable = false, length = 2048)
    private String requestUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_headers", columnDefinition = "json")
    private Map<String, Object> requestHeaders;

    @Column(name = "response_body", nullable = false, columnDefinition = "LONGTEXT")
    private String responseBody;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_metadata", columnDefinition = "json")
    private Map<String, Object> responseMetadata;
}
