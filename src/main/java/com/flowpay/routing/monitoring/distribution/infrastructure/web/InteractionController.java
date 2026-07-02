package com.flowpay.routing.monitoring.distribution.infrastructure.web;

import com.flowpay.routing.monitoring.distribution.domain.port.in.CreateInteraction;
import com.flowpay.routing.monitoring.distribution.domain.port.in.CreateInteraction.NewInteractionCommand;
import com.flowpay.routing.monitoring.distribution.domain.port.in.EndInteraction;
import com.flowpay.routing.monitoring.distribution.domain.port.in.InteractionView;
import com.flowpay.routing.monitoring.distribution.infrastructure.web.dto.CreateInteractionRequest;
import com.flowpay.routing.monitoring.distribution.infrastructure.web.dto.InteractionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** The two things the outside world can do: open a contact and close one. */
@RestController
@RequestMapping("/api/interactions")
public class InteractionController {

    private final CreateInteraction createInteraction;
    private final EndInteraction endInteraction;

    public InteractionController(CreateInteraction createInteraction, EndInteraction endInteraction) {
        this.createInteraction = createInteraction;
        this.endInteraction = endInteraction;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InteractionResponse create(@Valid @RequestBody CreateInteractionRequest request) {
        InteractionView view = createInteraction.handle(
                new NewInteractionCommand(request.customerName(), request.subject()));
        return InteractionResponse.from(view);
    }

    @PostMapping("/{id}/end")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void end(@PathVariable UUID id) {
        endInteraction.handle(id);
    }
}
