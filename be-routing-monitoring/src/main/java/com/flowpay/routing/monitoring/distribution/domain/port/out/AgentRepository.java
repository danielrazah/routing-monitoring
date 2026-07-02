package com.flowpay.routing.monitoring.distribution.domain.port.out;

import com.flowpay.routing.monitoring.distribution.domain.model.Agent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Loads agents (with their current load) and saves changes to it. */
public interface AgentRepository {

    /** All agents on a team, each already carrying the cases they are currently serving. */
    List<Agent> findByTeamName(String teamName);

    Optional<Agent> findById(UUID id);

    Agent save(Agent agent);
}
