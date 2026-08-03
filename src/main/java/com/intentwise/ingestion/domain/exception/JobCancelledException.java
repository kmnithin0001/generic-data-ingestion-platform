package com.intentwise.ingestion.domain.exception;

/**
 * Thrown when an active ingestion pipeline is cancelled by user request.
 */
public class JobCancelledException extends DomainException {
    public JobCancelledException(String message) {
        super(message);
    }
}
