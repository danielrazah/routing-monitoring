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
 * {@link #MAX_CONCURRENT} interactions at the same time. Because capacity is
 * checked inside {@link #assign}, there is no way to overload an agent from the outside.
 */
public class Agent {

    /** How many customers a single agent can serve simultaneously. */
    public static final int MAX_CONCURRENT = 3;

    private final UUID id;
    private final String name;
    private final UUID teamId;
    private final Set<UUID> activeInteractionIds; // the cases currently in this agent's hands

    public Agent(UUID id, String name, UUID teamId) {
        this(id, name, teamId, new HashSet<>());
    }

    /** Rebuild an agent from stored data, including the cases they are already serving. */
    public Agent(UUID id, String name, UUID teamId, Set<UUID> activeInteractionIds) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.teamId = Objects.requireNonNull(teamId, "teamId");
        this.activeInteractionIds = new HashSet<>(activeInteractionIds);
    }

    /** True while the agent still has room for one more customer. */
    public boolean hasFreeSlot() {
        return activeInteractionIds.size() < MAX_CONCURRENT;
    }

    /** How many customers the agent is serving right now. */
    public int currentLoad() {
        return activeInteractionIds.size();
    }

    /** Take on a customer. Refuses if the agent is already at the limit. */
    public void assign(Interaction interaction) {
        if (!hasFreeSlot()) {
            throw new AgentAtCapacityException(id, MAX_CONCURRENT);
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

    public Set<UUID> activeInteractionIds() {
        return Set.copyOf(activeInteractionIds);
    }
}
