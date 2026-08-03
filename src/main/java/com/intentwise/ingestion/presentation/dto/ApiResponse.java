package com.intentwise.ingestion.presentation.dto;

import java.time.LocalDateTime;

/**
 * Standard enterprise envelope for all successful REST API responses.
 *
 * @param <T> the type of the returned payload
 */
public record ApiResponse<T>(
    boolean success,
    LocalDateTime timestamp,
    String correlationId,
    String path,
    String requestId,
    T data
) {
    public static <T> ApiResponse<T> of(T data, String correlationId, String path, String requestId) {
        return new ApiResponse<>(true, LocalDateTime.now(), correlationId, path, requestId, data);
    }
}
