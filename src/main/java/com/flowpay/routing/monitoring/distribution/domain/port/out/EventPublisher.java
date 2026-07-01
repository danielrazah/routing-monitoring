package com.flowpay.routing.monitoring.distribution.domain.port.out;

import com.flowpay.routing.monitoring.distribution.domain.event.DomainEvent;

/**
 * Lets the core announce what happened without knowing who reacts to it.
 * This is why the domain never imports Spring or WebSocket: it just publishes;
 * an adapter on the edge decides to turn events into dashboard messages, logs, etc.
 */
public interface EventPublisher {

    void publish(DomainEvent event);
}
