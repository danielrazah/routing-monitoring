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
        UUID assignedAgentId) {

    public static InteractionResponse from(InteractionView view) {
        return new InteractionResponse(
                view.id(),
                view.customerName(),
                view.subject(),
                view.state(),
                view.assignedAgentId());
    }
}
