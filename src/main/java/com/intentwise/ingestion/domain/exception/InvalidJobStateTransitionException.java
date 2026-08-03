package com.intentwise.ingestion.domain.exception;

/**
 * Thrown when an invalid transition is attempted on a job's status.
 */
public class InvalidJobStateTransitionException extends DomainException {
    public InvalidJobStateTransitionException(String message) {
        super(message);
    }
}
