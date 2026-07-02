package com.flowpay.routing.monitoring.distribution.infrastructure.web;

import com.flowpay.routing.monitoring.distribution.domain.model.InteractionState;
import com.flowpay.routing.monitoring.distribution.infrastructure.config.DistributionProperties;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository.AgentJpaRepository;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository.InteractionJpaRepository;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository.QueueItemJpaRepository;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository.TeamJpaRepository;
import com.flowpay.routing.monitoring.distribution.infrastructure.web.dto.DashboardSnapshot;
import com.flowpay.routing.monitoring.distribution.infrastructure.web.dto.DashboardSnapshot.AgentSnapshot;
import com.flowpay.routing.monitoring.distribution.infrastructure.web.dto.DashboardSnapshot.TeamSnapshot;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity.TeamJpaEntity;

import java.util.List;
import java.util.UUID;

/**
 * Read-only endpoint that returns the current state of every team, agent and queue.
 * It's a straight query with no domain rules, so — pragmatically — it reads the JPA
 * repositories directly instead of going through a use-case port.
 *
 * <p>An AGENT only sees its own team: the token carries a {@code teamId} claim and we scope
 * the snapshot to it. ADMIN has no such claim and sees every team.
 */
@Tag(name = "Dashboard", description = "Read-only view of the whole operation")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final TeamJpaRepository teams;
    private final AgentJpaRepository agents;
    private final InteractionJpaRepository interactions;
    private final QueueItemJpaRepository queue;
    private final DistributionProperties properties;

    public DashboardController(TeamJpaRepository teams,
                               AgentJpaRepository agents,
                               InteractionJpaRepository interactions,
                               QueueItemJpaRepository queue,
                               DistributionProperties properties) {
        this.teams = teams;
        this.agents = agents;
        this.interactions = interactions;
        this.queue = queue;
        this.properties = properties;
    }

    @Operation(summary = "Current snapshot",
            description = "Teams with their agents' load, plus who is in service and who is waiting. "
                    + "An AGENT sees only its own team.")
    @GetMapping
    public DashboardSnapshot snapshot(@AuthenticationPrincipal Jwt jwt) {
        String teamId = jwt != null ? jwt.getClaimAsString("teamId") : null;
        List<TeamJpaEntity> scopedTeams = teamId == null
                ? teams.findAll()
                : teams.findById(UUID.fromString(teamId)).map(List::of).orElseGet(List::of);

        var teamSnapshots = scopedTeams.stream()
                .map(team -> new TeamSnapshot(
                        team.getId(),
                        team.getName(),
                        queue.countByTeamId(team.getId()),
                        agents.findByTeamId(team.getId()).stream()
                                .map(agent -> new AgentSnapshot(
                                        agent.getId(),
                                        agent.getName(),
                                        interactions.countByAssignedAgentIdAndState(
                                                agent.getId(), InteractionState.IN_SERVICE.name()),
                                        properties.getMaxConcurrentPerAgent(),
                                        interactions.findServingCustomerNamesByAgent(agent.getId())))
                                .toList(),
                        interactions.findServingCustomerNamesByTeam(team.getId()),
                        queue.findQueuedCustomerNamesByTeam(team.getId())))
                .toList();
        return new DashboardSnapshot(teamSnapshots);
    }
}
