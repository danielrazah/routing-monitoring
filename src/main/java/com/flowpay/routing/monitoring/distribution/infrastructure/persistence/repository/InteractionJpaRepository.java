package com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository;

import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity.InteractionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InteractionJpaRepository extends JpaRepository<InteractionJpaEntity, UUID> {

    /** The cases an agent is currently serving, used to reconstruct their live load. */
    List<InteractionJpaEntity> findByAssignedAgentIdAndState(UUID assignedAgentId, String state);
}
