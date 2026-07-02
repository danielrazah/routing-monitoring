package com.flowpay.routing.monitoring.distribution.domain.exception;

import java.util.UUID;

/** Raised when we try to hand an agent more interactions than they can serve at once. */
public class AgentAtCapacityException extends DomainException {
    public AgentAtCapacityException(UUID agentId, int limit) {
        super("Agent %s is already serving the maximum of %d interactions".formatted(agentId, limit));
    }
}
