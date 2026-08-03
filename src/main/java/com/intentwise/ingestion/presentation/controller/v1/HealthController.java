package com.intentwise.ingestion.presentation.controller.v1;

import com.intentwise.ingestion.presentation.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller exposing basic service wellness check endpoints.
 */
@Tag(name = "System Health", description = "Wellness check endpoints")
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @Operation(summary = "Get Health Status", description = "Checks whether the service is up and running.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Service is healthy and fully operational")
    })
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> checkHealth(HttpServletRequest servletRequest) {
        Map<String, String> status = Map.of("status", "UP");
        return ResponseEntity.ok(ApiResponse.of(status, MDC.get("correlationId"), servletRequest.getRequestURI(), MDC.get("requestId")));
    }
}
