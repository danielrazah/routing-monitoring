package com.flowpay.routing.monitoring.distribution.domain.model;

import com.flowpay.routing.monitoring.distribution.domain.exception.IllegalInteractionStateException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A single customer contact that a team needs to handle.
 * It knows its subject and where it is in its lifecycle, and it guards those
 * transitions itself so no one can, say, end a case that was never being served.
 */
public class Interaction {

    private final UUID id;
    private final String customerName;
    private final Subject subject;
    private final Instant createdAt;

    private InteractionState state;
    private UUID assignedAgentId; // null while the customer is still waiting

    /** New contact just arrived: it starts out waiting in line. */
    public Interaction(UUID id, String customerName, Subject subject) {
        this(id, customerName, subject, InteractionState.WAITING, null, Instant.now());
    }

    /** Rebuild an interaction from stored data. */
    public Interaction(UUID id, String customerName, Subject subject,
                       InteractionState state, UUID assignedAgentId, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.customerName = requireText(customerName);
        this.subject = Objects.requireNonNull(subject, "subject");
        this.state = Objects.requireNonNull(state, "state");
        this.assignedAgentId = assignedAgentId;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    /** An agent picked this up: it moves into service and remembers who is serving it. */
    void startWith(UUID agentId) {
        transitionTo(InteractionState.IN_SERVICE);
        this.assignedAgentId = Objects.requireNonNull(agentId, "agentId");
    }

    /** The customer was helped and the case is closed for good. */
    public void end() {
        transitionTo(InteractionState.ENDED);
    }

    private void transitionTo(InteractionState next) {
        if (!state.canTransitionTo(next)) {
            throw new IllegalInteractionStateException(id, state, next);
        }
        this.state = next;
    }

    private static String requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("customerName must not be blank");
        }
        return value;
    }

    public UUID id() {
        return id;
    }

    public String customerName() {
        return customerName;
    }

    public Subject subject() {
        return subject;
    }

    public InteractionState state() {
        return state;
    }

    public UUID assignedAgentId() {
        return assignedAgentId;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
