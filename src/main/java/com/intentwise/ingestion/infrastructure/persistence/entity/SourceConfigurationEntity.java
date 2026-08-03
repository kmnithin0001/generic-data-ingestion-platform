package com.intentwise.ingestion.infrastructure.persistence.entity;

import com.intentwise.ingestion.domain.model.AuthenticationType;
import com.intentwise.ingestion.domain.model.HttpMethodType;
import com.intentwise.ingestion.domain.model.PaginationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

/**
 * JPA entity representing the `source_configurations` table.
 * Uses Hibernate 6 SQL JSON type mappings for nested options and configurations.
 */
@Entity
@Table(name = "source_configurations")
@Getter
@Setter
public class SourceConfigurationEntity extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "url", nullable = false, length = 1024)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 10)
    private HttpMethodType method;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", nullable = false, length = 32)
    private AuthenticationType authType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "auth_config", columnDefinition = "json")
    private Map<String, Object> authConfig;

    @Enumerated(EnumType.STRING)
    @Column(name = "pagination_type", nullable = false, length = 32)
    private PaginationType paginationType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pagination_config", columnDefinition = "json")
    private Map<String, Object> paginationConfig;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_options", columnDefinition = "json")
    private Map<String, Object> requestOptions;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
