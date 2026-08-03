package com.intentwise.ingestion.presentation.dto;

import com.intentwise.ingestion.domain.model.RawApiResponse;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable response representing a raw API response page payload.
 */
public record RawResponseDto(
    UUID id,
    int pageNumber,
    String requestUrl,
    String responseBody,
    LocalDateTime createdAt
) {
    public static RawResponseDto fromDomain(RawApiResponse res) {
        if (res == null) return null;
        return new RawResponseDto(
            res.getId(),
            res.getPageNumber(),
            res.getRequestUrl(),
            res.getResponseBody(),
            res.getCreatedAt()
        );
    }
}
