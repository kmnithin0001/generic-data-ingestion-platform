package com.intentwise.ingestion.presentation.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard enterprise envelope for all error responses in the system.
 */
public record ApiErrorResponse(
    boolean success,
    String errorCode,
    String message,
    List<String> details,
    LocalDateTime timestamp,
    String correlationId,
    String path,
    String requestId
) {
    public static ApiErrorResponse of(String errorCode, String message, List<String> details, String correlationId, String path, String requestId) {
        return new ApiErrorResponse(false, errorCode, message, details, LocalDateTime.now(), correlationId, path, requestId);
    }
}
