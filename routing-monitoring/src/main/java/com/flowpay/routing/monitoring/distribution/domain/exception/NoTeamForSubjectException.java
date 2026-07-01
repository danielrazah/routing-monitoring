package com.flowpay.routing.monitoring.distribution.domain.exception;

import com.flowpay.routing.monitoring.distribution.domain.model.Subject;

/** Raised when no team accepts a subject. In practice the catch-all team prevents this. */
public class NoTeamForSubjectException extends DomainException {
    public NoTeamForSubjectException(Subject subject) {
        super("No team is responsible for subject " + subject);
    }
}
