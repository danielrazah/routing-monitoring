package com.flowpay.routing.monitoring.distribution.infrastructure.persistence.mapper;

import com.flowpay.routing.monitoring.distribution.domain.model.Agent;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity.AgentJpaEntity;

import java.util.Set;
import java.util.UUID;

public final class AgentMapper {

    private AgentMapper() {
    }

    /**
     * Rebuild an agent together with the cases they are serving right now. Those ids come
     * from the interaction table (state IN_SERVICE), which is what defines the agent's load.
     * The concurrency limit comes from configuration.
     */
    public static Agent toDomain(AgentJpaEntity entity, int maxConcurrent, Set<UUID> activeInteractionIds) {
        return new Agent(entity.getId(), entity.getName(), entity.getTeamId(), maxConcurrent, activeInteractionIds);
    }
}
