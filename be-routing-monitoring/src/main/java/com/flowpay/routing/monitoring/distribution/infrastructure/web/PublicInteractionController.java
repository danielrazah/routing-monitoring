package com.flowpay.routing.monitoring.distribution.infrastructure.web;

import com.flowpay.routing.monitoring.distribution.domain.exception.InteractionNotFoundException;
import com.flowpay.routing.monitoring.distribution.domain.model.Interaction;
import com.flowpay.routing.monitoring.distribution.domain.port.in.CreateInteraction;
import com.flowpay.routing.monitoring.distribution.domain.port.in.CreateInteraction.NewInteractionCommand;
import com.flowpay.routing.monitoring.distribution.domain.port.in.InteractionView;
import com.flowpay.routing.monitoring.distribution.domain.port.out.InteractionRepository;
import com.flowpay.routing.monitoring.distribution.infrastructure.web.dto.CreateInteractionRequest;
import com.flowpay.routing.monitoring.distribution.infrastructure.web.dto.InteractionResponse;
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

    public PublicInteractionController(CreateInteraction createInteraction, InteractionRepository interactions) {
        this.createInteraction = createInteraction;
        this.interactions = interactions;
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
}
