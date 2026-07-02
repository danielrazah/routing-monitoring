package com.flowpay.routing.monitoring.distribution.domain.service;

import com.flowpay.routing.monitoring.distribution.domain.model.Agent;
import com.flowpay.routing.monitoring.distribution.domain.model.Interaction;

import java.util.Comparator;
import java.util.List;

/**
 * The heart of the policy: hand the interaction to a free agent, or, if the whole
 * team is busy, tell the caller to queue it.
 *
 * When more than one agent is free we pick the least loaded one, so work spreads
 * evenly instead of piling onto whoever comes first in the list.
 *
 * This is pure domain logic: no database, no framework. It just decides and mutates
 * the chosen agent; persisting and actually enqueueing is the caller's job.
 */
public class DistributionService {

    public DistributionResult distribute(Interaction interaction, List<Agent> teamAgents) {
        return teamAgents.stream()
                .filter(Agent::hasFreeSlot)
                .min(Comparator.comparingInt(Agent::currentLoad))
                .map(agent -> {
                    agent.assign(interaction);
                    return DistributionResult.assignedTo(agent);
                })
                .orElseGet(DistributionResult::queued);
    }
}
