package com.intentwise.ingestion.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Pure domain model representing an API source endpoint configuration.
 * Captures connection details, security schemes, and pagination options.
 * This model is completely decoupled from Spring and database-specific annotations.
 */
@Value
@Builder(toBuilder = true)
public class SourceConfiguration {
    UUID id;
    String name;
    String url;
    HttpMethodType method;
    AuthenticationType authType;
    Map<String, Object> authConfig;
    PaginationType paginationType;
    Map<String, Object> paginationConfig;
    Map<String, Object> requestOptions;
    boolean active;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
