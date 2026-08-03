package com.intentwise.ingestion.domain.model;

/**
 * Immutable configuration details for API Key authentication.
 * Validates itself on construction and masks secrets in its toString representation.
 */
public record ApiKeyConfig(String keyName, String keyValue, String placement) {

    /**
     * Compact constructor validating configurations.
     */
    public ApiKeyConfig {
        if (keyName == null || keyName.trim().isEmpty()) {
            throw new IllegalArgumentException("API Key Name must not be null or blank");
        }
        if (keyValue == null || keyValue.trim().isEmpty()) {
            throw new IllegalArgumentException("API Key Value must not be null or blank");
        }
        if (placement == null || (!placement.equalsIgnoreCase("HEADER") && !placement.equalsIgnoreCase("QUERY"))) {
            throw new IllegalArgumentException("API Key Placement must be 'HEADER' or 'QUERY'");
        }
    }

    @Override
    public String toString() {
        return "ApiKeyConfig[keyName=" + keyName + ", keyValue=********, placement=" + placement + "]";
    }
}
