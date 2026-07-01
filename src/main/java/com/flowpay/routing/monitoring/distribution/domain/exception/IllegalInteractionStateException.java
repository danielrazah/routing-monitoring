package com.flowpay.routing.monitoring.distribution.domain.exception;

import com.flowpay.routing.monitoring.distribution.domain.model.InteractionState;

import java.util.UUID;

/** Raised when an interaction is asked to make a move its lifecycle doesn't allow. */
public class IllegalInteractionStateException extends DomainException {
    public IllegalInteractionStateException(UUID interactionId, InteractionState from, InteractionState to) {
        super("Interaction %s cannot move from %s to %s".formatted(interactionId, from, to));
    }
}
