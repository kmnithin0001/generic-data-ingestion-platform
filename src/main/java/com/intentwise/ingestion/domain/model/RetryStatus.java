package com.intentwise.ingestion.domain.model;

/**
 * Captures status information of request retries inside the execution engine.
 */
public enum RetryStatus {
    /**
     * Initial attempt has not required any retry.
     */
    PENDING,

    /**
     * The system is currently executing retries for a transient failure.
     */
    RETRYING,

    /**
     * Retries were completed successfully and connection was restored.
     */
    SUCCESS,

    /**
     * All retries have exhausted and failed.
     */
    FAILED
}
