package com.flowpay.routing.monitoring.distribution.domain.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * The line of customers waiting for a team when every agent is busy.
 * It's first-in, first-out: whoever has been waiting longest is served next.
 *
 * This in-memory model captures the queueing rule itself. In production the line is
 * kept in the database (so it survives restarts and many workers can drain it safely),
 * but the fairness rule it represents is exactly this one.
 */
public class WaitingQueue {

    private final UUID teamId;
    private final Deque<Interaction> waiting = new ArrayDeque<>();

    public WaitingQueue(UUID teamId) {
        this.teamId = Objects.requireNonNull(teamId, "teamId");
    }

    /** A customer joins the back of the line. */
    public void enqueue(Interaction interaction) {
        waiting.addLast(Objects.requireNonNull(interaction, "interaction"));
    }

    /** Take the customer who has been waiting longest, if there is one. */
    public Optional<Interaction> dequeue() {
        return Optional.ofNullable(waiting.pollFirst());
    }

    public boolean isEmpty() {
        return waiting.isEmpty();
    }

    public int size() {
        return waiting.size();
    }

    public UUID teamId() {
        return teamId;
    }
}
