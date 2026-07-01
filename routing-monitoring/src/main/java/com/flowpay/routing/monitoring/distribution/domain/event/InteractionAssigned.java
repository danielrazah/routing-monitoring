package com.flowpay.routing.monitoring.distribution.domain.event;

import java.time.Instant;
import java.util.UUID;

/** An agent took the interaction and is now serving the customer. */
public record InteractionAssigned(UUID interactionId, UUID agentId, UUID teamId, Instant occurredAt)
        implements DomainEvent {

    public static InteractionAssigned now(UUID interactionId, UUID agentId, UUID teamId) {
        return new InteractionAssigned(interactionId, agentId, teamId, Instant.now());
    }
}
