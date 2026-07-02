package com.flowpay.routing.monitoring.distribution.domain.port.in;

import com.flowpay.routing.monitoring.distribution.domain.model.Interaction;
import com.flowpay.routing.monitoring.distribution.domain.model.InteractionState;
import com.flowpay.routing.monitoring.distribution.domain.model.Subject;

import java.util.UUID;

/** A small read-only snapshot of an interaction, returned by the use cases. */
public record InteractionView(
        UUID id,
        String customerName,
        Subject subject,
        InteractionState state,
        UUID assignedAgentId) {

    public static InteractionView of(Interaction interaction) {
        return new InteractionView(
                interaction.id(),
                interaction.customerName(),
                interaction.subject(),
                interaction.state(),
                interaction.assignedAgentId());
    }
}
