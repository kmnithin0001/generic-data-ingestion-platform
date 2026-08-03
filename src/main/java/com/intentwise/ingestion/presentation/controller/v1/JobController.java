package com.intentwise.ingestion.presentation.controller.v1;

import com.intentwise.ingestion.application.usecase.CancelJobUseCase;
import com.intentwise.ingestion.application.usecase.GetJobStatusUseCase;
import com.intentwise.ingestion.application.usecase.GetRawResponsesUseCase;
import com.intentwise.ingestion.application.usecase.ListJobsUseCase;
import com.intentwise.ingestion.application.usecase.RetryIngestionUseCase;
import com.intentwise.ingestion.domain.model.IngestionJob;
import com.intentwise.ingestion.domain.model.JobFilter;
import com.intentwise.ingestion.domain.model.JobStatus;
import com.intentwise.ingestion.domain.model.PageResult;
import com.intentwise.ingestion.presentation.dto.ApiResponse;
import com.intentwise.ingestion.presentation.dto.JobResponse;
import com.intentwise.ingestion.presentation.dto.RawResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller exposing endpoints for querying, retrying, and cancelling ingestion jobs.
 */
@Tag(name = "Jobs Management", description = "Endpoints for managing ingestion job executions")
@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final RetryIngestionUseCase retryIngestionUseCase;
    private final CancelJobUseCase cancelJobUseCase;
    private final GetJobStatusUseCase getJobStatusUseCase;
    private final ListJobsUseCase listJobsUseCase;
    private final GetRawResponsesUseCase getRawResponsesUseCase;

    @Operation(summary = "Get Job details", description = "Retrieves current status, metrics, and progress for a Job ID.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Job not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{jobId}")
    public ResponseEntity<ApiResponse<JobResponse>> getJob(
            @PathVariable UUID jobId,
            HttpServletRequest servletRequest) {
        IngestionJob job = getJobStatusUseCase.execute(jobId);
        return ResponseEntity.ok(ApiResponse.of(JobResponse.fromDomain(job), MDC.get("correlationId"), servletRequest.getRequestURI(), MDC.get("requestId")));
    }

    @Operation(summary = "Retry Ingestion Job", description = "Restarts a failed or cancelled ingestion job asynchronously.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Retry job accepted and starting"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Job is currently active"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Cannot retry completed job"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Job not found")
    })
    @PostMapping("/{jobId}/retry")
    public ResponseEntity<ApiResponse<JobResponse>> retryJob(
            @PathVariable UUID jobId,
            HttpServletRequest servletRequest) {
        String corrIdStr = MDC.get("correlationId");
        UUID correlationId = corrIdStr != null ? UUID.fromString(corrIdStr) : UUID.randomUUID();
        IngestionJob newJob = retryIngestionUseCase.execute(jobId, correlationId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.of(JobResponse.fromDomain(newJob), corrIdStr, servletRequest.getRequestURI(), MDC.get("requestId")));
    }

    @Operation(summary = "Cancel Ingestion Job", description = "Aborts an active ingestion job gracefully.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Job successfully marked for cancellation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Job is already completed or cancelled"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Job not found")
    })
    @PostMapping("/{jobId}/cancel")
    public ResponseEntity<ApiResponse<JobResponse>> cancelJob(
            @PathVariable UUID jobId,
            HttpServletRequest servletRequest) {
        String corrIdStr = MDC.get("correlationId");
        UUID correlationId = corrIdStr != null ? UUID.fromString(corrIdStr) : UUID.randomUUID();
        IngestionJob cancelledJob = cancelJobUseCase.execute(jobId, correlationId);
        return ResponseEntity.ok(ApiResponse.of(JobResponse.fromDomain(cancelledJob), corrIdStr, servletRequest.getRequestURI(), MDC.get("requestId")));
    }

    @Operation(summary = "List Jobs", description = "Lists ingestion jobs matching optional criteria with pagination.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<JobResponse>>> listJobs(
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) UUID sourceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdBefore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest servletRequest) {

        JobFilter filter = new JobFilter(status, sourceId, createdAfter, createdBefore);
        PageResult<IngestionJob> domainPage = listJobsUseCase.execute(filter, page, size);

        List<JobResponse> content = domainPage.content().stream()
                .map(JobResponse::fromDomain)
                .collect(Collectors.toList());

        PageResult<JobResponse> resultPage = new PageResult<>(
                content,
                domainPage.totalElements(),
                domainPage.page(),
                domainPage.size(),
                domainPage.totalPages(),
                domainPage.hasNext(),
                domainPage.hasPrevious()
        );

        return ResponseEntity.ok(ApiResponse.of(resultPage, MDC.get("correlationId"), servletRequest.getRequestURI(), MDC.get("requestId")));
    }

    @Operation(summary = "Get Raw API Responses", description = "Retrieves raw response payloads collected during job execution.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping("/{jobId}/responses")
    public ResponseEntity<ApiResponse<List<RawResponseDto>>> getRawResponses(
            @PathVariable UUID jobId,
            HttpServletRequest servletRequest) {
        List<RawResponseDto> responses = getRawResponsesUseCase.execute(jobId).stream()
                .map(RawResponseDto::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.of(responses, MDC.get("correlationId"), servletRequest.getRequestURI(), MDC.get("requestId")));
    }
}
