package com.flowpay.routing.monitoring.distribution.domain.exception;

import java.util.UUID;

/** Raised when we look up an interaction that doesn't exist. */
public class InteractionNotFoundException extends DomainException {
    public InteractionNotFoundException(UUID interactionId) {
        super("Interaction %s was not found".formatted(interactionId));
    }
}
