package com.flowpay.routing.monitoring.distribution.domain.port.in;

import com.flowpay.routing.monitoring.distribution.domain.model.Subject;

/**
 * Use case: a new customer contact arrives. It gets routed to a team and then either
 * handed to a free agent or placed in the team's waiting line.
 */
public interface CreateInteraction {

    InteractionView handle(NewInteractionCommand command);

    /** What the caller must provide to open a new contact. */
    record NewInteractionCommand(String customerName, Subject subject) {
    }
}
