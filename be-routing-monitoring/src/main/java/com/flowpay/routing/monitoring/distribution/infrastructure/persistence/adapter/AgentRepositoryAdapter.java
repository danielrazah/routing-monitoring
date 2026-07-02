package com.flowpay.routing.monitoring.distribution.infrastructure.persistence.adapter;

import com.flowpay.routing.monitoring.distribution.domain.model.Agent;
import com.flowpay.routing.monitoring.distribution.domain.model.InteractionState;
import com.flowpay.routing.monitoring.distribution.domain.port.out.AgentRepository;
import com.flowpay.routing.monitoring.distribution.infrastructure.config.DistributionProperties;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity.AgentJpaEntity;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity.InteractionJpaEntity;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.mapper.AgentMapper;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository.AgentJpaRepository;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository.InteractionJpaRepository;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository.TeamJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class AgentRepositoryAdapter implements AgentRepository {

    private final AgentJpaRepository agents;
    private final TeamJpaRepository teams;
    private final InteractionJpaRepository interactions;
    private final DistributionProperties properties;

    public AgentRepositoryAdapter(AgentJpaRepository agents,
                                  TeamJpaRepository teams,
                                  InteractionJpaRepository interactions,
                                  DistributionProperties properties) {
        this.agents = agents;
        this.teams = teams;
        this.interactions = interactions;
        this.properties = properties;
    }

    @Override
    public List<Agent> findByTeamName(String teamName) {
        return teams.findByName(teamName)
                .map(team -> agents.findByTeamId(team.getId()).stream()
                        .map(this::withCurrentLoad)
                        .toList())
                .orElse(List.of());
    }

    @Override
    public Optional<Agent> findById(UUID id) {
        return agents.findById(id).map(this::withCurrentLoad);
    }

    @Override
    public Agent save(Agent agent) {
        // Nothing to write here: an agent's load is derived from the interactions assigned to
        // them, and those are saved through the InteractionRepository. This keeps the two from
        // ever disagreeing. The method stays on the port for symmetry and future fields.
        return agent;
    }

    /** Load the agent together with the interactions they are currently serving. */
    private Agent withCurrentLoad(AgentJpaEntity entity) {
        Set<UUID> active = interactions
                .findByAssignedAgentIdAndState(entity.getId(), InteractionState.IN_SERVICE.name())
                .stream()
                .map(InteractionJpaEntity::getId)
                .collect(Collectors.toSet());
        return AgentMapper.toDomain(entity, properties.getMaxConcurrentPerAgent(), active);
    }
}
