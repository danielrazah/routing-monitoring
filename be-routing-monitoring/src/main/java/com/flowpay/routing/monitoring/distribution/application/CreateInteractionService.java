package com.flowpay.routing.monitoring.distribution.application;

import com.flowpay.routing.monitoring.distribution.domain.event.InteractionAssigned;
import com.flowpay.routing.monitoring.distribution.domain.event.InteractionCreated;
import com.flowpay.routing.monitoring.distribution.domain.event.InteractionQueued;
import com.flowpay.routing.monitoring.distribution.domain.exception.TeamNotFoundException;
import com.flowpay.routing.monitoring.distribution.domain.model.Agent;
import com.flowpay.routing.monitoring.distribution.domain.model.Interaction;
import com.flowpay.routing.monitoring.distribution.domain.model.Team;
import com.flowpay.routing.monitoring.distribution.domain.port.in.CreateInteraction;
import com.flowpay.routing.monitoring.distribution.domain.port.in.InteractionView;
import com.flowpay.routing.monitoring.distribution.domain.port.out.AgentRepository;
import com.flowpay.routing.monitoring.distribution.domain.port.out.EventPublisher;
import com.flowpay.routing.monitoring.distribution.domain.port.out.InteractionRepository;
import com.flowpay.routing.monitoring.distribution.domain.port.out.TeamRepository;
import com.flowpay.routing.monitoring.distribution.domain.port.out.WaitingQueuePort;
import com.flowpay.routing.monitoring.distribution.domain.routing.SubjectRouter;
import com.flowpay.routing.monitoring.distribution.domain.service.DistributionResult;
import com.flowpay.routing.monitoring.distribution.domain.service.DistributionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Handles a brand-new contact end to end: route it to a team, then either hand it
 * to a free agent or drop it into the team's line. Wrapped in a transaction so the
 * interaction, the agent's load and the queue always change together.
 */
@Service
public class CreateInteractionService implements CreateInteraction {

    private static final Logger log = LoggerFactory.getLogger(CreateInteractionService.class);

    private final SubjectRouter router;
    private final DistributionService distribution;
    private final TeamRepository teams;
    private final AgentRepository agents;
    private final InteractionRepository interactions;
    private final WaitingQueuePort queue;
    private final EventPublisher events;

    public CreateInteractionService(SubjectRouter router,
                                    DistributionService distribution,
                                    TeamRepository teams,
                                    AgentRepository agents,
                                    InteractionRepository interactions,
                                    WaitingQueuePort queue,
                                    EventPublisher events) {
        this.router = router;
        this.distribution = distribution;
        this.teams = teams;
        this.agents = agents;
        this.interactions = interactions;
        this.queue = queue;
        this.events = events;
    }

    @Override
    @Transactional
    public InteractionView handle(NewInteractionCommand command) {
        String teamName = router.routeToTeam(command.subject());
        Team team = teams.findByName(teamName)
                .orElseThrow(() -> new TeamNotFoundException(teamName));

        Interaction interaction = new Interaction(UUID.randomUUID(), command.customerName(), command.subject());
        events.publish(InteractionCreated.now(interaction.id(), interaction.subject(), team.id()));
        log.info("Interaction {} created for '{}' ({}) -> team {}",
                interaction.id(), command.customerName(), command.subject(), team.name());

        List<Agent> teamAgents = agents.findByTeamName(team.name());
        DistributionResult result = distribution.distribute(interaction, teamAgents);

        interactions.save(interaction);
        switch (result) {
            case DistributionResult.Assigned assigned -> {
                agents.save(assigned.agent());
                events.publish(InteractionAssigned.now(interaction.id(), assigned.agent().id(), team.id()));
                log.info("Interaction {} assigned to agent {}", interaction.id(), assigned.agent().id());
            }
            case DistributionResult.Queued ignored -> {
                queue.enqueue(team.id(), interaction.id());
                events.publish(InteractionQueued.now(interaction.id(), team.id()));
                log.info("Interaction {} queued on team {} (all agents at capacity)",
                        interaction.id(), team.name());
            }
        }

        return InteractionView.of(interaction);
    }
}
