package com.flowpay.routing.monitoring.distribution.domain.model;

import com.flowpay.routing.monitoring.distribution.domain.exception.AgentAtCapacityException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * A person on a team who serves customers.
 *
 * The heart of the distribution rule lives here: an agent handles at most
 * {@code maxConcurrent} interactions at the same time. That limit is configurable
 * (see the {@code distribution.max-concurrent-per-agent} property); it defaults to
 * {@link #DEFAULT_MAX_CONCURRENT}. Because capacity is checked inside {@link #assign},
 * there is no way to overload an agent from the outside.
 */
public class Agent {

    /** Default number of customers a single agent can serve at once. */
    public static final int DEFAULT_MAX_CONCURRENT = 3;

    private final UUID id;
    private final String name;
    private final UUID teamId;
    private final int maxConcurrent;
    private final Set<UUID> activeInteractionIds; // the cases currently in this agent's hands

    public Agent(UUID id, String name, UUID teamId) {
        this(id, name, teamId, DEFAULT_MAX_CONCURRENT, new HashSet<>());
    }

    /** Rebuild an agent from stored data, including the cases they are already serving. */
    public Agent(UUID id, String name, UUID teamId, int maxConcurrent, Set<UUID> activeInteractionIds) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.teamId = Objects.requireNonNull(teamId, "teamId");
        if (maxConcurrent < 1) {
            throw new IllegalArgumentException("maxConcurrent must be at least 1");
        }
        this.maxConcurrent = maxConcurrent;
        this.activeInteractionIds = new HashSet<>(activeInteractionIds);
    }

    /** True while the agent still has room for one more customer. */
    public boolean hasFreeSlot() {
        return activeInteractionIds.size() < maxConcurrent;
    }

    /** How many customers the agent is serving right now. */
    public int currentLoad() {
        return activeInteractionIds.size();
    }

    /** Take on a customer. Refuses if the agent is already at the limit. */
    public void assign(Interaction interaction) {
        if (!hasFreeSlot()) {
            throw new AgentAtCapacityException(id, maxConcurrent);
        }
        interaction.startWith(id);
        activeInteractionIds.add(interaction.id());
    }

    /** Free a slot once a case is closed, so the agent can pick up the next person in line. */
    public void release(UUID interactionId) {
        activeInteractionIds.remove(interactionId);
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public UUID teamId() {
        return teamId;
    }

    public int maxConcurrent() {
        return maxConcurrent;
    }

    public Set<UUID> activeInteractionIds() {
        return Set.copyOf(activeInteractionIds);
    }
}
