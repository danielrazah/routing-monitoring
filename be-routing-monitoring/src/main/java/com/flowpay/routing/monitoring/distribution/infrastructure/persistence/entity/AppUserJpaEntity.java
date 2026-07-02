package com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * A login account. Its role decides what the user may do: ADMIN operates everything, AGENT
 * only reads the dashboard of the team it belongs to ({@link #teamId}, null for ADMIN).
 */
@Entity
@Table(name = "app_user")
public class AppUserJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role;

    /** The team an AGENT belongs to; null for ADMIN (who sees every team). */
    @Column(name = "team_id")
    private UUID teamId;

    /** The agent this login represents; null for ADMIN. */
    @Column(name = "agent_id")
    private UUID agentId;

    @Column(nullable = false)
    private boolean enabled;

    protected AppUserJpaEntity() {
        // required by JPA
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public UUID getAgentId() {
        return agentId;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
