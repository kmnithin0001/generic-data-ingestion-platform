package com.intentwise.ingestion.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Pure domain model representing the raw content and metadata captured from a single API call.
 * This is saved into a JSON column format to preserve exactly what was sent and received.
 */
@Value
@Builder(toBuilder = true)
public class RawApiResponse {
    UUID id;
    UUID jobId;
    Integer pageNumber;
    String requestUrl;
    Map<String, Object> requestHeaders;
    String responseBody;
    Map<String, Object> responseMetadata;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
