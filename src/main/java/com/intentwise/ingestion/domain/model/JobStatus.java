package com.intentwise.ingestion.domain.model;

/**
 * Represents the current lifecycle state of an ingestion execution job.
 */
public enum JobStatus {
    /**
     * Job is registered in the system but has not yet started execution.
     */
    PENDING,

    /**
     * Job is currently active and executing the ingestion pipeline.
     */
    RUNNING,

    /**
     * Job successfully finished ingesting all available data pages.
     */
    COMPLETED,

    /**
     * Job encountered an unrecoverable failure during execution.
     */
    FAILED,

    /**
     * Job was cancelled by user request.
     */
    CANCELLED
}
