package com.flowpay.routing.monitoring.distribution.infrastructure.web;

import com.flowpay.routing.monitoring.distribution.domain.exception.InteractionNotFoundException;
import com.flowpay.routing.monitoring.distribution.domain.model.InteractionState;
import com.flowpay.routing.monitoring.distribution.domain.port.in.EndInteraction;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity.AppUserJpaEntity;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity.InteractionJpaEntity;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository.AppUserJpaRepository;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository.InteractionJpaRepository;
import com.flowpay.routing.monitoring.distribution.infrastructure.web.dto.ConversationSummary;
import com.flowpay.routing.monitoring.distribution.infrastructure.web.dto.MessageResponse;
import com.flowpay.routing.monitoring.distribution.infrastructure.web.dto.SendMessageRequest;
import com.flowpay.routing.monitoring.distribution.infrastructure.websocket.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * The agent side of the chat. An agent sees the customers it is serving right now (one dialog
 * each) and exchanges messages on each interaction. Ownership is enforced from the login's
 * agent (app_user.agent_id): an agent may only touch interactions assigned to it. ADMIN, which
 * has no agent, isn't personally serving anyone.
 */
@Tag(name = "Agent chat", description = "The agent's live conversations with the customers it serves")
@RestController
public class AgentChatController {

    private static final Logger log = LoggerFactory.getLogger(AgentChatController.class);

    private final ChatService chat;
    private final InteractionJpaRepository interactions;
    private final AppUserJpaRepository users;
    private final EndInteraction endInteraction;

    public AgentChatController(ChatService chat, InteractionJpaRepository interactions,
                              AppUserJpaRepository users, EndInteraction endInteraction) {
        this.chat = chat;
        this.interactions = interactions;
        this.users = users;
        this.endInteraction = endInteraction;
    }

    @Operation(summary = "My open conversations",
            description = "The interactions this agent is serving now, one dialog each.")
    @GetMapping("/api/agent/conversations")
    public List<ConversationSummary> conversations(@AuthenticationPrincipal Jwt jwt) {
        UUID agentId = callerAgentId(jwt);
        if (agentId == null) {
            return List.of(); // ADMIN isn't personally serving anyone
        }
        return interactions.findByAssignedAgentIdAndState(agentId, InteractionState.IN_SERVICE.name()).stream()
                .map(i -> new ConversationSummary(i.getId(), i.getCustomerName()))
                .toList();
    }

    @Operation(summary = "Thread of one conversation")
    @GetMapping("/api/interactions/{id}/messages")
    public List<MessageResponse> thread(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        requireOwnership(id, jwt);
        return chat.thread(id);
    }

    @Operation(summary = "Reply to the customer", description = "Posts a message as the agent.")
    @PostMapping("/api/interactions/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse send(@PathVariable UUID id, @Valid @RequestBody SendMessageRequest request,
                                @AuthenticationPrincipal Jwt jwt) {
        requireOwnership(id, jwt);
        return chat.post(id, ChatService.AGENT, request.body());
    }

    @Operation(summary = "End one of my conversations",
            description = "Closes the interaction this agent is serving, freeing its slot and pulling "
                    + "the next customer in line. An agent may only end interactions assigned to it.")
    @PostMapping("/api/agent/conversations/{id}/end")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void end(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        requireOwnership(id, jwt);
        log.info("Agent {} ending interaction {}", jwt.getSubject(), id);
        endInteraction.handle(id);
    }

    private UUID callerAgentId(Jwt jwt) {
        return users.findByUsername(jwt.getSubject()).map(AppUserJpaEntity::getAgentId).orElse(null);
    }

    /** An AGENT may only touch its own interactions; ADMIN (no agent) may touch any. */
    private void requireOwnership(UUID interactionId, Jwt jwt) {
        InteractionJpaEntity interaction = interactions.findById(interactionId)
                .orElseThrow(() -> new InteractionNotFoundException(interactionId));
        UUID agentId = callerAgentId(jwt);
        if (agentId != null && !agentId.equals(interaction.getAssignedAgentId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This interaction is not assigned to you");
        }
    }
}
