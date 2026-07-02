package com.flowpay.routing.monitoring.distribution.infrastructure.web;

import com.flowpay.routing.monitoring.distribution.domain.exception.InteractionNotFoundException;
import com.flowpay.routing.monitoring.distribution.domain.model.Interaction;
import com.flowpay.routing.monitoring.distribution.domain.port.in.CreateInteraction;
import com.flowpay.routing.monitoring.distribution.domain.port.in.CreateInteraction.NewInteractionCommand;
import com.flowpay.routing.monitoring.distribution.domain.port.in.InteractionView;
import com.flowpay.routing.monitoring.distribution.domain.port.out.InteractionRepository;
import com.flowpay.routing.monitoring.distribution.infrastructure.web.dto.CreateInteractionRequest;
import com.flowpay.routing.monitoring.distribution.infrastructure.web.dto.InteractionResponse;
import com.flowpay.routing.monitoring.distribution.infrastructure.web.dto.MessageResponse;
import com.flowpay.routing.monitoring.distribution.infrastructure.web.dto.SendMessageRequest;
import com.flowpay.routing.monitoring.distribution.infrastructure.websocket.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Public, no-login endpoints for the customer-facing queue screen. A customer opens a
 * contact (joining the queue like any other interaction) and polls its status to know when
 * they've been picked up. It reuses the exact same {@link CreateInteraction} use case as the
 * dashboard — same routing, same max-3 rule, same queue — just without authentication.
 */
@Tag(name = "Public queue", description = "No-login endpoints for customers to join and track the queue")
@RestController
@RequestMapping("/api/public/interactions")
public class PublicInteractionController {

    private final CreateInteraction createInteraction;
    private final InteractionRepository interactions;
    private final ChatService chat;

    public PublicInteractionController(CreateInteraction createInteraction,
                                       InteractionRepository interactions,
                                       ChatService chat) {
        this.createInteraction = createInteraction;
        this.interactions = interactions;
        this.chat = chat;
    }

    @Operation(summary = "Join the queue as a new contact",
            description = "Same as opening an interaction: it is routed to a team and either "
                    + "assigned to a free agent or put in the waiting line.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InteractionResponse join(@Valid @RequestBody CreateInteractionRequest request) {
        InteractionView view = createInteraction.handle(
                new NewInteractionCommand(request.customerName(), request.subject()));
        return InteractionResponse.from(view);
    }

    @Operation(summary = "Check my interaction",
            description = "Lets the customer screen see when it flips from WAITING to IN_SERVICE.")
    @GetMapping("/{id}")
    public InteractionResponse status(@PathVariable UUID id) {
        Interaction interaction = interactions.findById(id)
                .orElseThrow(() -> new InteractionNotFoundException(id));
        return InteractionResponse.from(InteractionView.of(interaction));
    }

    @Operation(summary = "My chat thread", description = "All messages exchanged on this interaction.")
    @GetMapping("/{id}/messages")
    public List<MessageResponse> messages(@PathVariable UUID id) {
        requireInteraction(id);
        return chat.thread(id);
    }

    @Operation(summary = "Send a message", description = "Posts a message as the customer.")
    @PostMapping("/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse send(@PathVariable UUID id, @Valid @RequestBody SendMessageRequest request) {
        requireInteraction(id);
        return chat.post(id, ChatService.CUSTOMER, request.body());
    }

    private void requireInteraction(UUID id) {
        if (interactions.findById(id).isEmpty()) {
            throw new InteractionNotFoundException(id);
        }
    }
}
