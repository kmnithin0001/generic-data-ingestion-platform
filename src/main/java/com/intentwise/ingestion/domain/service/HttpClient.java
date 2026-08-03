package com.intentwise.ingestion.domain.service;

import com.intentwise.ingestion.domain.model.RequestExecutionResult;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Interface representing a decoupled HTTP Client.
 * Eliminates direct dependencies on specific HTTP clients (e.g. WebClient, RestTemplate, HttpClient).
 */
public interface HttpClient {

    /**
     * Executes an HTTP request and wraps the outcome into a RequestExecutionResult.
     *
     * @param url         the absolute URL endpoint
     * @param method      the HTTP method (GET, POST, etc.)
     * @param headers     a map of HTTP headers
     * @param queryParams a map of query parameters
     * @param body        the string payload, or null if GET
     * @param timeout     the connection read timeout duration
     * @return a RequestExecutionResult encapsulating response status, headers, and body
     */
    RequestExecutionResult execute(
        String url,
        String method,
        Map<String, List<String>> headers,
        Map<String, List<String>> queryParams,
        String body,
        Duration timeout
    );
}
