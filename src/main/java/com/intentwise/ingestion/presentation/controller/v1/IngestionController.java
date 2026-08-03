package com.intentwise.ingestion.presentation.controller.v1;

import com.intentwise.ingestion.application.usecase.StartIngestionUseCase;
import com.intentwise.ingestion.domain.model.IngestionJob;
import com.intentwise.ingestion.domain.model.JobStatus;
import com.intentwise.ingestion.domain.model.SourceConfiguration;
import com.intentwise.ingestion.presentation.dto.ApiResponse;
import com.intentwise.ingestion.presentation.dto.JobResponse;
import com.intentwise.ingestion.presentation.dto.StartIngestionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controller exposing endpoints for starting data ingestion workflows.
 */
@Tag(name = "Ingestion Engine", description = "Endpoints for orchestrating ingestion tasks")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class IngestionController {

    private final StartIngestionUseCase startIngestionUseCase;

    @Operation(summary = "Start Ingestion", description = "Initiates a data ingestion job asynchronously. Supports optional Idempotency-Key.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Ingestion job successfully accepted and started"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Active or completed job returned via Idempotency match"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request arguments or body validation failed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/ingest")
    public ResponseEntity<ApiResponse<JobResponse>> startIngestion(
            @Valid @RequestBody StartIngestionRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            HttpServletRequest servletRequest) {

        String corrIdStr = MDC.get("correlationId");
        UUID correlationId = corrIdStr != null ? UUID.fromString(corrIdStr) : UUID.randomUUID();
        String requestId = MDC.get("requestId");

        // Map DTO to Domain Model
        SourceConfiguration sourceConfig = SourceConfiguration.builder()
                .name(request.name())
                .url(request.url())
                .method(request.method())
                .authType(request.authType())
                .authConfig(request.authConfig())
                .paginationType(request.paginationType())
                .paginationConfig(request.paginationConfig())
                .requestOptions(request.requestOptions())
                .active(true)
                .build();

        IngestionJob job = startIngestionUseCase.execute(sourceConfig, idempotencyKey, correlationId);
        JobResponse jobResponse = JobResponse.fromDomain(job);
        ApiResponse<JobResponse> apiResponse = ApiResponse.of(jobResponse, corrIdStr, servletRequest.getRequestURI(), requestId);

        // If the job already exists (status != PENDING on initial creation), return 200 OK.
        // Otherwise return 202 Accepted for a brand new asynchronous ingestion.
        if (job.getStatus() == JobStatus.PENDING) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(apiResponse);
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
        }
    }
}
