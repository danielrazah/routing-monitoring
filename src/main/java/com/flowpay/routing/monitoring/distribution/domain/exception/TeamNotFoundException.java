package com.flowpay.routing.monitoring.distribution.domain.exception;

/** Raised when routing points at a team that isn't registered. */
public class TeamNotFoundException extends DomainException {
    public TeamNotFoundException(String teamName) {
        super("Team '%s' was not found".formatted(teamName));
    }
}
