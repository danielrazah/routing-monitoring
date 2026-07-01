package com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository;

import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity.AgentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AgentJpaRepository extends JpaRepository<AgentJpaEntity, UUID> {

    List<AgentJpaEntity> findByTeamId(UUID teamId);
}
