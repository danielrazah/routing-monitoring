package com.flowpay.routing.monitoring.distribution.domain.exception;

import java.util.UUID;

/** Raised when a team has no ongoing interaction, so there's no slot to free. */
public class QueueAdvanceNotPossibleException extends DomainException {
    public QueueAdvanceNotPossibleException(UUID teamId) {
        super("Team %s has no interaction in service to free".formatted(teamId));
    }
}
