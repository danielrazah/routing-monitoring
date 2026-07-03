package com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** One chat message on an interaction, sent by the CUSTOMER or the AGENT. */
@Entity
@Table(name = "message")
public class MessageJpaEntity {

    @Id
    private UUID id;

    @Column(name = "interaction_id", nullable = false)
    private UUID interactionId;

    @Column(nullable = false)
    private String sender;

    @Column(nullable = false)
    private String body;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected MessageJpaEntity() {
        // required by JPA
    }

    public MessageJpaEntity(UUID id, UUID interactionId, String sender, String body, Instant createdAt) {
        this.id = id;
        this.interactionId = interactionId;
        this.sender = sender;
        this.body = body;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getInteractionId() {
        return interactionId;
    }

    public String getSender() {
        return sender;
    }

    public String getBody() {
        return body;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
