package com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** JPA view of an interaction. Subject and state are stored as their enum names. */
@Entity
@Table(name = "interaction")
public class InteractionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String state;

    @Column(name = "assigned_agent_id")
    private UUID assignedAgentId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected InteractionJpaEntity() {
        // required by JPA
    }

    public InteractionJpaEntity(UUID id, String customerName, String subject, String state,
                                UUID assignedAgentId, Instant createdAt) {
        this.id = id;
        this.customerName = customerName;
        this.subject = subject;
        this.state = state;
        this.assignedAgentId = assignedAgentId;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getSubject() {
        return subject;
    }

    public String getState() {
        return state;
    }

    public UUID getAssignedAgentId() {
        return assignedAgentId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
