package com.flowpay.routing.monitoring.distribution.infrastructure.websocket;

import com.flowpay.routing.monitoring.distribution.domain.event.DomainEvent;
import com.flowpay.routing.monitoring.distribution.domain.event.InteractionAssigned;
import com.flowpay.routing.monitoring.distribution.domain.event.InteractionCreated;
import com.flowpay.routing.monitoring.distribution.domain.event.InteractionEnded;
import com.flowpay.routing.monitoring.distribution.domain.event.InteractionQueued;

import java.time.Instant;
import java.util.UUID;

/**
 * The flat shape a domain event takes on the wire to the dashboard. Fields that don't
 * apply to a given event stay null (e.g. no agent when a customer is queued).
 */
public record DashboardMessage(
        String type,
        UUID interactionId,
        UUID teamId,
        UUID agentId,
        String subject,
        Instant occurredAt) {

    public static DashboardMessage from(DomainEvent event) {
        return switch (event) {
            case InteractionCreated e ->
                    new DashboardMessage("CREATED", e.interactionId(), e.teamId(), null, e.subject().name(), e.occurredAt());
            case InteractionAssigned e ->
                    new DashboardMessage("ASSIGNED", e.interactionId(), e.teamId(), e.agentId(), null, e.occurredAt());
            case InteractionQueued e ->
                    new DashboardMessage("QUEUED", e.interactionId(), e.teamId(), null, null, e.occurredAt());
            case InteractionEnded e ->
                    new DashboardMessage("ENDED", e.interactionId(), e.teamId(), e.agentId(), null, e.occurredAt());
        };
    }
}
