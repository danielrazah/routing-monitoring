package com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository;

import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity.QueueItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface QueueItemJpaRepository extends JpaRepository<QueueItemJpaEntity, UUID> {

    /**
     * Lock and return the oldest waiting item for a team.
     *
     * FOR UPDATE SKIP LOCKED is the key: when several agents finish at the same moment,
     * each locks a different row and moves on, instead of everyone fighting over (and
     * accidentally double-serving) the same waiting customer.
     */
    @Query(value = """
            SELECT * FROM interaction_queue
            WHERE team_id = :teamId
            ORDER BY enqueued_at
            FOR UPDATE SKIP LOCKED
            LIMIT 1
            """, nativeQuery = true)
    Optional<QueueItemJpaEntity> lockNextForTeam(@Param("teamId") UUID teamId);
}
