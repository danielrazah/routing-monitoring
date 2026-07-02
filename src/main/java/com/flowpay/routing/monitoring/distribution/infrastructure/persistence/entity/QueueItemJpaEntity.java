package com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** One customer waiting in a team's line. Rows are consumed oldest-first. */
@Entity
@Table(name = "interaction_queue")
public class QueueItemJpaEntity {

    @Id
    private UUID id;

    @Column(name = "team_id", nullable = false)
    private UUID teamId;

    @Column(name = "interaction_id", nullable = false, unique = true)
    private UUID interactionId;

    @Column(name = "enqueued_at", nullable = false)
    private Instant enqueuedAt;

    protected QueueItemJpaEntity() {
        // required by JPA
    }

    public QueueItemJpaEntity(UUID id, UUID teamId, UUID interactionId, Instant enqueuedAt) {
        this.id = id;
        this.teamId = teamId;
        this.interactionId = interactionId;
        this.enqueuedAt = enqueuedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public UUID getInteractionId() {
        return interactionId;
    }

    public Instant getEnqueuedAt() {
        return enqueuedAt;
    }
}
