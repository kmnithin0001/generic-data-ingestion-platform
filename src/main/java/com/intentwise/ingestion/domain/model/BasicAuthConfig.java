package com.intentwise.ingestion.domain.model;

/**
 * Immutable configuration details for HTTP Basic Authentication.
 * Validates itself on construction and masks secrets in its toString representation.
 */
public record BasicAuthConfig(String username, String password) {

    /**
     * Compact constructor validating configurations.
     */
    public BasicAuthConfig {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Basic Auth Username must not be null or blank");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Basic Auth Password must not be null or blank");
        }
    }

    @Override
    public String toString() {
        return "BasicAuthConfig[username=" + username + ", password=********]";
    }
}
