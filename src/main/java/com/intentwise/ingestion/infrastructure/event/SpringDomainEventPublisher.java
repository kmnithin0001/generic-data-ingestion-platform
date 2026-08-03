package com.intentwise.ingestion.infrastructure.event;

import com.intentwise.ingestion.application.event.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Spring-backed implementation of the DomainEventPublisher port.
 * Delegates event publishing to Spring's ApplicationEventPublisher.
 */
@Component
@RequiredArgsConstructor
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publish(Object event) {
        if (event != null) {
            eventPublisher.publishEvent(event);
        }
    }
}
