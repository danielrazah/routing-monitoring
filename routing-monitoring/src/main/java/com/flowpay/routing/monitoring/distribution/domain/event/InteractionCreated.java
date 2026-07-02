package com.flowpay.routing.monitoring.distribution.domain.event;

import com.flowpay.routing.monitoring.distribution.domain.model.Subject;

import java.time.Instant;
import java.util.UUID;

/** A new contact arrived and was routed to a team (not yet assigned or queued). */
public record InteractionCreated(UUID interactionId, Subject subject, UUID teamId, Instant occurredAt)
        implements DomainEvent {

    public static InteractionCreated now(UUID interactionId, Subject subject, UUID teamId) {
        return new InteractionCreated(interactionId, subject, teamId, Instant.now());
    }
}
