package com.intentwise.ingestion.domain.exception;

/**
 * Thrown when an operation or transition is attempted on an already cancelled job.
 */
public class JobAlreadyCancelledException extends DomainException {
    public JobAlreadyCancelledException(String message) {
        super(message);
    }
}
