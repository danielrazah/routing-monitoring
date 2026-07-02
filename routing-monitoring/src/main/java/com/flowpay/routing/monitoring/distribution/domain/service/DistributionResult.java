package com.flowpay.routing.monitoring.distribution.domain.service;

import com.flowpay.routing.monitoring.distribution.domain.model.Agent;

/**
 * The outcome of trying to place an interaction: either an agent took it,
 * or the whole team was busy and it has to wait in line.
 */
public sealed interface DistributionResult {

    /** An agent had a free slot and is now serving the customer. */
    record Assigned(Agent agent) implements DistributionResult {
    }

    /** Everyone was busy; the customer must be queued. */
    record Queued() implements DistributionResult {
    }

    static DistributionResult assignedTo(Agent agent) {
        return new Assigned(agent);
    }

    static DistributionResult queued() {
        return new Queued();
    }
}
