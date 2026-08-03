package com.intentwise.ingestion.presentation.exception;

import com.intentwise.ingestion.domain.exception.DomainException;
import com.intentwise.ingestion.domain.exception.InvalidJobStateTransitionException;
import com.intentwise.ingestion.domain.exception.JobAlreadyCancelledException;
import com.intentwise.ingestion.domain.exception.JobAlreadyCompletedException;
import com.intentwise.ingestion.presentation.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global REST exception handler translating all validation, domain, and database errors
 * to structured ApiErrorResponse envelopes.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String CORRELATION_ID_MDC = "correlationId";
    private static final String REQUEST_ID_MDC = "requestId";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation error occurred on path: {}", request.getRequestURI());
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.toList());

        ApiErrorResponse body = ApiErrorResponse.of(
                "VALIDATION_ERROR",
                "Request validation failed",
                details,
                getMdc(CORRELATION_ID_MDC),
                request.getRequestURI(),
                getMdc(REQUEST_ID_MDC)
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(InvalidJobStateTransitionException.class)
    public ResponseEntity<ApiErrorResponse> handleStateTransitionException(InvalidJobStateTransitionException ex, HttpServletRequest request) {
        log.warn("Invalid state transition attempted: {}", ex.getMessage());
        ApiErrorResponse body = ApiErrorResponse.of(
                "VALIDATION_ERROR",
                ex.getMessage(),
                List.of(ex.getMessage()),
                getMdc(CORRELATION_ID_MDC),
                request.getRequestURI(),
                getMdc(REQUEST_ID_MDC)
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler({JobAlreadyCancelledException.class, JobAlreadyCompletedException.class})
    public ResponseEntity<ApiErrorResponse> handleTerminalStateException(DomainException ex, HttpServletRequest request) {
        log.warn("Operation attempted on terminal job state: {}", ex.getMessage());
        String code = ex instanceof JobAlreadyCancelledException ? "JOB_CANCELLED" : "VALIDATION_ERROR";
        ApiErrorResponse body = ApiErrorResponse.of(
                code,
                ex.getMessage(),
                List.of(ex.getMessage()),
                getMdc(CORRELATION_ID_MDC),
                request.getRequestURI(),
                getMdc(REQUEST_ID_MDC)
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiErrorResponse> handleDomainException(DomainException ex, HttpServletRequest request) {
        log.error("Domain exception occurred: {}", ex.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String errorCode = "VALIDATION_ERROR";

        if (ex.getMessage().contains("not found")) {
            status = HttpStatus.NOT_FOUND;
            errorCode = "RESOURCE_NOT_FOUND";
        }

        ApiErrorResponse body = ApiErrorResponse.of(
                errorCode,
                ex.getMessage(),
                List.of(ex.getMessage()),
                getMdc(CORRELATION_ID_MDC),
                request.getRequestURI(),
                getMdc(REQUEST_ID_MDC)
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument exception: {}", ex.getMessage());
        ApiErrorResponse body = ApiErrorResponse.of(
                "VALIDATION_ERROR",
                ex.getMessage(),
                List.of(ex.getMessage()),
                getMdc(CORRELATION_ID_MDC),
                request.getRequestURI(),
                getMdc(REQUEST_ID_MDC)
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected internal server error on path: {}", request.getRequestURI(), ex);
        ApiErrorResponse body = ApiErrorResponse.of(
                "INTERNAL_ERROR",
                "An unexpected internal error occurred",
                List.of(ex.getMessage() != null ? ex.getMessage() : "No message provided"),
                getMdc(CORRELATION_ID_MDC),
                request.getRequestURI(),
                getMdc(REQUEST_ID_MDC)
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private String getMdc(String key) {
        String val = MDC.get(key);
        return val != null ? val : "N/A";
    }
}
