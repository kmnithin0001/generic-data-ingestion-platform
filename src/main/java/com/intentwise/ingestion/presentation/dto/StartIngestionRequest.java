package com.intentwise.ingestion.presentation.dto;

import com.intentwise.ingestion.domain.model.AuthenticationType;
import com.intentwise.ingestion.domain.model.HttpMethodType;
import com.intentwise.ingestion.domain.model.PaginationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

import java.util.Map;

/**
 * Immutable request payload for starting a new ingestion job.
 */
public record StartIngestionRequest(
    @NotBlank(message = "Configuration name must not be blank")
    String name,

    @NotBlank(message = "Base URL must not be blank")
    @URL(message = "Base URL must be a valid HTTP or HTTPS URL")
    String url,

    @NotNull(message = "HTTP Method must not be null")
    HttpMethodType method,

    @NotNull(message = "Authentication Type must not be null")
    AuthenticationType authType,

    Map<String, Object> authConfig,

    @NotNull(message = "Pagination Type must not be null")
    PaginationType paginationType,

    Map<String, Object> paginationConfig,

    Map<String, Object> requestOptions
) {}
