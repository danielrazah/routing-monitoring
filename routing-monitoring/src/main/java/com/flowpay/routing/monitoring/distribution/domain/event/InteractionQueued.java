package com.flowpay.routing.monitoring.distribution.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Every agent on the team was busy, so the customer was placed in the waiting line. */
public record InteractionQueued(UUID interactionId, UUID teamId, Instant occurredAt)
        implements DomainEvent {

    public static InteractionQueued now(UUID interactionId, UUID teamId) {
        return new InteractionQueued(interactionId, teamId, Instant.now());
    }
}
