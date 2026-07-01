package com.flowpay.routing.monitoring.distribution.infrastructure.websocket;

import com.flowpay.routing.monitoring.distribution.domain.event.DomainEvent;
import com.flowpay.routing.monitoring.distribution.domain.port.out.EventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Bridges the domain's EventPublisher port onto Spring's own event bus. This is the one
 * place that knows both worlds, so the core can stay unaware of Spring entirely.
 */
@Component
public class SpringEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher delegate;

    public SpringEventPublisher(ApplicationEventPublisher delegate) {
        this.delegate = delegate;
    }

    @Override
    public void publish(DomainEvent event) {
        delegate.publishEvent(event);
    }
}
