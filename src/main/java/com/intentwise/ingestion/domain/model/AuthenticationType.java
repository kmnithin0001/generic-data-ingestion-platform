package com.intentwise.ingestion.domain.model;

/**
 * Enumerates the supported security schemes for connecting to target REST APIs.
 */
public enum AuthenticationType {
    /**
     * No authentication required.
     */
    NONE,

    /**
     * API Key based authentication passed as custom request headers or query params.
     */
    API_KEY,

    /**
     * Bearer token based authorization (using the HTTP Authorization: Bearer Header).
     */
    BEARER,

    /**
     * HTTP Basic Access Authentication (using Base64 encoded Username:Password credentials).
     */
    BASIC,

    /**
     * Reserved for future OAuth2 Client Credentials or Authorization Code flow authentication.
     */
    OAUTH2
}
