package com.flowpay.routing.monitoring.distribution.infrastructure.web;

import com.flowpay.routing.monitoring.distribution.domain.model.InteractionState;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity.AgentJpaEntity;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity.InteractionJpaEntity;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity.TeamJpaEntity;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository.AgentJpaRepository;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository.InteractionJpaRepository;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository.QueueItemJpaRepository;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository.TeamJpaRepository;
import com.flowpay.routing.monitoring.distribution.infrastructure.web.dto.AdminConversation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ADMIN-only operations that cut across teams. Two things live here:
 *
 * <ul>
 *   <li><b>Monitor</b> — a read-only list of every live customer↔agent conversation, so an admin
 *       can watch all dialogs in real time (subscribing to each thread's {@code /topic/chat.*}).</li>
 *   <li><b>Reset</b> — a testing convenience that ends every open interaction and empties every
 *       queue, returning the whole board to zero.</li>
 * </ul>
 *
 * <p>Both are plain queries/commands with no domain rules, so — as with the dashboard snapshot —
 * they read the JPA repositories directly. Access is gated to ADMIN in {@code SecurityConfig}.
 */
@Tag(name = "Admin", description = "ADMIN-only cross-team monitoring and reset")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final InteractionJpaRepository interactions;
    private final AgentJpaRepository agents;
    private final TeamJpaRepository teams;
    private final QueueItemJpaRepository queue;

    public AdminController(InteractionJpaRepository interactions,
                           AgentJpaRepository agents,
                           TeamJpaRepository teams,
                           QueueItemJpaRepository queue) {
        this.interactions = interactions;
        this.agents = agents;
        this.teams = teams;
        this.queue = queue;
    }

    @Operation(summary = "All live conversations",
            description = "Every in-service interaction with its customer, serving agent and team, "
                    + "so an admin can watch all dialogs at once.")
    @GetMapping("/conversations")
    public List<AdminConversation> conversations() {
        Map<UUID, AgentJpaEntity> agentsById = agents.findAll().stream()
                .collect(Collectors.toMap(AgentJpaEntity::getId, Function.identity()));
        Map<UUID, String> teamNameById = teams.findAll().stream()
                .collect(Collectors.toMap(TeamJpaEntity::getId, TeamJpaEntity::getName));

        return interactions.findByState(InteractionState.IN_SERVICE.name()).stream()
                .map(i -> {
                    AgentJpaEntity agent = agentsById.get(i.getAssignedAgentId());
                    UUID teamId = agent != null ? agent.getTeamId() : null;
                    return new AdminConversation(
                            i.getId(),
                            i.getCustomerName(),
                            agent != null ? agent.getId() : null,
                            agent != null ? agent.getName() : null,
                            teamId,
                            teamId != null ? teamNameById.get(teamId) : null);
                })
                .toList();
    }

    @Operation(summary = "Reset the whole board",
            description = "Ends every open interaction and empties every queue so all slots are free. "
                    + "A convenience for testing — ADMIN only.")
    @PostMapping("/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void reset(@AuthenticationPrincipal Jwt jwt) {
        // Empty the queues first so ending the in-service interactions can't pull anyone back in.
        queue.deleteAllInBatch();
        int closed = interactions.endAllActive();
        log.info("Admin {} reset the board: cleared all queues and ended {} open interaction(s)",
                jwt != null ? jwt.getSubject() : "?", closed);
    }
}
