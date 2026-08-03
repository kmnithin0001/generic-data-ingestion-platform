package com.intentwise.ingestion.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the network result, payload sizes, and metrics returned from an HTTP API call.
 * This is a domain-level abstraction to keep HTTP client dependencies out of the business engine.
 */
@Value
@Builder
public class RequestExecutionResult {
    int statusCode;
    Map<String, List<String>> headers;
    String responseBody;
    Duration executionTime;
    String requestUrl;
    int pageNumber;
    long requestSizeBytes;
    long responseSizeBytes;
}
