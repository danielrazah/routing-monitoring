package com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/** JPA view of a team. Kept separate from the domain Team on purpose. */
@Entity
@Table(name = "team")
public class TeamJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    protected TeamJpaEntity() {
        // required by JPA
    }

    public TeamJpaEntity(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
