package com.flowpay.routing.monitoring.distribution.domain.event;

import java.time.Instant;
import java.util.UUID;

/** A case was closed, freeing a slot on the agent (which may pull the next in line). */
public record InteractionEnded(UUID interactionId, UUID agentId, UUID teamId, Instant occurredAt)
        implements DomainEvent {

    public static InteractionEnded now(UUID interactionId, UUID agentId, UUID teamId) {
        return new InteractionEnded(interactionId, agentId, teamId, Instant.now());
    }
}
