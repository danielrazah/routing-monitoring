package com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * JPA view of an agent. Note it doesn't store the current load: that is derived from
 * how many interactions are in service for this agent, so it can never drift out of sync.
 */
@Entity
@Table(name = "agent")
public class AgentJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "team_id", nullable = false)
    private UUID teamId;

    protected AgentJpaEntity() {
        // required by JPA
    }

    public AgentJpaEntity(UUID id, String name, UUID teamId) {
        this.id = id;
        this.name = name;
        this.teamId = teamId;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getTeamId() {
        return teamId;
    }
}
