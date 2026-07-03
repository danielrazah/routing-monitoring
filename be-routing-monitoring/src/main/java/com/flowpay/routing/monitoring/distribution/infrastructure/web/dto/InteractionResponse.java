package com.flowpay.routing.monitoring.distribution.infrastructure.web.dto;

import com.flowpay.routing.monitoring.distribution.domain.model.InteractionState;
import com.flowpay.routing.monitoring.distribution.domain.model.Subject;
import com.flowpay.routing.monitoring.distribution.domain.port.in.InteractionView;

import java.util.UUID;

/** What the API returns for an interaction. Deliberately not the JPA entity. */
public record InteractionResponse(
        UUID id,
        String customerName,
        Subject subject,
        InteractionState state,
        UUID assignedAgentId,
        String assignedAgentName) {

    public static InteractionResponse from(InteractionView view) {
        return from(view, null);
    }

    /** Same, but resolving the serving agent's name so the customer screen can show who it is. */
    public static InteractionResponse from(InteractionView view, String assignedAgentName) {
        return new InteractionResponse(
                view.id(),
                view.customerName(),
                view.subject(),
                view.state(),
                view.assignedAgentId(),
                assignedAgentName);
    }
}
