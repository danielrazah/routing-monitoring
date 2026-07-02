package com.flowpay.routing.monitoring.distribution.infrastructure.persistence.adapter;

import com.flowpay.routing.monitoring.distribution.domain.port.out.WaitingQueuePort;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity.QueueItemJpaEntity;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository.QueueItemJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Database-backed waiting line. The {@code pollNext} runs inside the caller's transaction:
 * it locks the oldest row with SKIP LOCKED, deletes it and hands back the interaction id,
 * so the same waiting customer can never be picked up twice.
 */
@Repository
public class WaitingQueueAdapter implements WaitingQueuePort {

    private final QueueItemJpaRepository jpa;

    public WaitingQueueAdapter(QueueItemJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void enqueue(UUID teamId, UUID interactionId) {
        jpa.save(new QueueItemJpaEntity(UUID.randomUUID(), teamId, interactionId, Instant.now()));
    }

    @Override
    public Optional<UUID> pollNext(UUID teamId) {
        return jpa.lockNextForTeam(teamId).map(item -> {
            jpa.delete(item);
            return item.getInteractionId();
        });
    }
}
