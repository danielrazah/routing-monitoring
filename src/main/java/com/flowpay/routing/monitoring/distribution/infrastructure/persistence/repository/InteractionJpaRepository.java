package com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository;

import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity.InteractionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InteractionJpaRepository extends JpaRepository<InteractionJpaEntity, UUID> {

    /** The cases an agent is currently serving, used to reconstruct their live load. */
    List<InteractionJpaEntity> findByAssignedAgentIdAndState(UUID assignedAgentId, String state);

    /** How many cases an agent is serving right now, for the dashboard snapshot. */
    long countByAssignedAgentIdAndState(UUID assignedAgentId, String state);

    /** The team's oldest ongoing interaction (joins through the assigned agent's team). */
    @Query(value = """
            SELECT i.* FROM interaction i
            JOIN agent a ON a.id = i.assigned_agent_id
            WHERE a.team_id = :teamId AND i.state = 'IN_SERVICE'
            ORDER BY i.created_at
            LIMIT 1
            """, nativeQuery = true)
    Optional<InteractionJpaEntity> findOldestInServiceByTeam(@Param("teamId") UUID teamId);
}
