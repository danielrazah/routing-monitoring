package com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository;

import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity.MessageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageJpaRepository extends JpaRepository<MessageJpaEntity, UUID> {

    /** The full thread of one interaction, oldest message first. */
    List<MessageJpaEntity> findByInteractionIdOrderByCreatedAtAsc(UUID interactionId);
}
