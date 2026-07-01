package com.flowpay.routing.monitoring.distribution.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * A group of agents specialized in a kind of subject (Cards, Loans, ...).
 * Teams are just an identity here; their agents are loaded on demand by the use cases.
 * New teams can be introduced without touching the existing ones.
 */
public class Team {

    private final UUID id;
    private final String name;

    public Team(UUID id, String name) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }
}
