package com.intentwise.ingestion.domain.model;

/**
 * Immutable configuration details for Bearer token authorization.
 * Validates itself on construction and masks secrets in its toString representation.
 */
public record BearerTokenConfig(String token) {

    /**
     * Compact constructor validating configurations.
     */
    public BearerTokenConfig {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Bearer Token must not be null or blank");
        }
    }

    @Override
    public String toString() {
        return "BearerTokenConfig[token=********]";
    }
}
