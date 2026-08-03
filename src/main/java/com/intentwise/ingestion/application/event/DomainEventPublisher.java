package com.intentwise.ingestion.application.event;

/**
 * Application port interface responsible for publishing domain events,
 * keeping the domain and application layers isolated from direct Spring Event references.
 */
public interface DomainEventPublisher {

    /**
     * Publishes a domain event.
     *
     * @param event the event object to publish
     */
    void publish(Object event);
}
