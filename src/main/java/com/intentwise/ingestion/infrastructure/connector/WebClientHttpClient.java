package com.intentwise.ingestion.infrastructure.connector;

import com.intentwise.ingestion.domain.model.RequestExecutionResult;
import com.intentwise.ingestion.domain.service.HttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * WebClient-based implementation of HttpClient.
 * Executes REST calls using Reactor WebClient. Captures execution times,
 * header configurations, and converts WebClientResponseExceptions to result models.
 */
@Component
@RequiredArgsConstructor
public class WebClientHttpClient implements HttpClient {

    private final WebClient.Builder webClientBuilder;

    @Override
    public RequestExecutionResult execute(
            String url,
            String method,
            Map<String, List<String>> headers,
            Map<String, List<String>> queryParams,
            String body,
            Duration timeout) {

        // Construct URL with query parameters
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
        if (queryParams != null) {
            queryParams.forEach((key, values) -> {
                if (values != null) {
                    values.forEach(val -> uriBuilder.queryParam(key, val));
                }
            });
        }
        String targetUri = uriBuilder.encode().toUriString();

        WebClient client = webClientBuilder.build();
        HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
        WebClient.RequestBodySpec requestSpec = client.method(httpMethod).uri(targetUri);

        // Apply headers
        if (headers != null) {
            headers.forEach((key, values) -> {
                if (values != null) {
                    values.forEach(val -> requestSpec.header(key, val));
                }
            });
        }

        // Apply body if present
        if (body != null && !body.trim().isEmpty() && httpMethod != HttpMethod.GET) {
            requestSpec.bodyValue(body);
        }

        long requestSizeBytes = body != null ? body.getBytes(StandardCharsets.UTF_8).length : 0L;
        Instant start = Instant.now();

        try {
            // Perform execution blocking on the thread
            ResponseEntity<String> response = requestSpec.retrieve()
                    .toEntity(String.class)
                    .block(timeout != null ? timeout : Duration.ofSeconds(15));

            Instant end = Instant.now();
            Duration duration = Duration.between(start, end);

            int statusCode = response != null ? response.getStatusCode().value() : 500;
            Map<String, List<String>> responseHeaders = response != null ? response.getHeaders() : Map.of();
            String responseBody = response != null ? response.getBody() : "";
            long responseSizeBytes = responseBody != null ? responseBody.getBytes(StandardCharsets.UTF_8).length : 0L;

            return RequestExecutionResult.builder()
                    .statusCode(statusCode)
                    .headers(responseHeaders)
                    .responseBody(responseBody)
                    .executionTime(duration)
                    .requestUrl(targetUri)
                    .requestSizeBytes(requestSizeBytes)
                    .responseSizeBytes(responseSizeBytes)
                    .build();

        } catch (WebClientResponseException e) {
            // Map non-2xx HTTP responses directly to execution results for pipeline validation
            Instant end = Instant.now();
            Duration duration = Duration.between(start, end);
            String responseBody = e.getResponseBodyAsString();
            long responseSizeBytes = responseBody != null ? responseBody.getBytes(StandardCharsets.UTF_8).length : 0L;

            return RequestExecutionResult.builder()
                    .statusCode(e.getStatusCode().value())
                    .headers(e.getHeaders())
                    .responseBody(responseBody)
                    .executionTime(duration)
                    .requestUrl(targetUri)
                    .requestSizeBytes(requestSizeBytes)
                    .responseSizeBytes(responseSizeBytes)
                    .build();

        } catch (Exception e) {
            throw new IllegalStateException("HTTP communication failure to URL: " + targetUri, e);
        }
    }
}
