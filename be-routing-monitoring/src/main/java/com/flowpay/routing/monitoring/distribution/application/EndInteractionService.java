package com.flowpay.routing.monitoring.distribution.application;

import com.flowpay.routing.monitoring.distribution.domain.event.InteractionAssigned;
import com.flowpay.routing.monitoring.distribution.domain.event.InteractionEnded;
import com.flowpay.routing.monitoring.distribution.domain.exception.AgentNotFoundException;
import com.flowpay.routing.monitoring.distribution.domain.exception.InteractionNotFoundException;
import com.flowpay.routing.monitoring.distribution.domain.model.Agent;
import com.flowpay.routing.monitoring.distribution.domain.model.Interaction;
import com.flowpay.routing.monitoring.distribution.domain.port.in.EndInteraction;
import com.flowpay.routing.monitoring.distribution.domain.port.out.AgentRepository;
import com.flowpay.routing.monitoring.distribution.domain.port.out.EventPublisher;
import com.flowpay.routing.monitoring.distribution.domain.port.out.InteractionRepository;
import com.flowpay.routing.monitoring.distribution.domain.port.out.WaitingQueuePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Closes an interaction and, because a slot just opened, immediately serves the next
 * customer waiting for that team. All in one transaction so we never free a slot
 * without also pulling the next person (or leave someone half-assigned on failure).
 */
@Service
public class EndInteractionService implements EndInteraction {

    private static final Logger log = LoggerFactory.getLogger(EndInteractionService.class);

    private final InteractionRepository interactions;
    private final AgentRepository agents;
    private final WaitingQueuePort queue;
    private final EventPublisher events;

    public EndInteractionService(InteractionRepository interactions,
                                 AgentRepository agents,
                                 WaitingQueuePort queue,
                                 EventPublisher events) {
        this.interactions = interactions;
        this.agents = agents;
        this.queue = queue;
        this.events = events;
    }

    @Override
    @Transactional
    public void handle(UUID interactionId) {
        Interaction interaction = interactions.findById(interactionId)
                .orElseThrow(() -> new InteractionNotFoundException(interactionId));

        // end() only succeeds if the interaction was actually in service, so the agent id is set.
        interaction.end();
        UUID agentId = interaction.assignedAgentId();
        interactions.save(interaction);

        Agent agent = agents.findById(agentId)
                .orElseThrow(() -> new AgentNotFoundException(agentId));
        agent.release(interactionId);
        UUID teamId = agent.teamId();
        events.publish(InteractionEnded.now(interactionId, agentId, teamId));
        log.info("Interaction {} ended; freed a slot on agent {} (team {})", interactionId, agentId, teamId);

        // The slot that just opened goes to whoever has been waiting longest for this team.
        // SKIP LOCKED in the adapter makes sure two agents finishing at once don't grab the same person.
        queue.pollNext(teamId).ifPresent(nextInteractionId -> {
            Interaction next = interactions.findById(nextInteractionId)
                    .orElseThrow(() -> new InteractionNotFoundException(nextInteractionId));
            agent.assign(next);
            interactions.save(next);
            events.publish(InteractionAssigned.now(next.id(), agent.id(), teamId));
            log.info("Interaction {} pulled from the queue into service on agent {}", next.id(), agent.id());
        });

        agents.save(agent);
    }
}
