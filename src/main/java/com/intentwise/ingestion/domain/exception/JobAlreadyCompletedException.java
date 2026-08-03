package com.intentwise.ingestion.domain.exception;

/**
 * Thrown when an operation or transition is attempted on an already completed job.
 */
public class JobAlreadyCompletedException extends DomainException {
    public JobAlreadyCompletedException(String message) {
        super(message);
    }
}
