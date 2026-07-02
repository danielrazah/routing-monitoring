package com.flowpay.routing.monitoring.distribution.domain.exception;

import java.util.UUID;

/** Raised when the agent tied to an interaction can't be found. */
public class AgentNotFoundException extends DomainException {
    public AgentNotFoundException(UUID agentId) {
        super("Agent %s was not found".formatted(agentId));
    }
}
