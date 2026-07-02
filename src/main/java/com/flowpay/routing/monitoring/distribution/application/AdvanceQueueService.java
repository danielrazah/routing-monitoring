package com.flowpay.routing.monitoring.distribution.application;

import com.flowpay.routing.monitoring.distribution.domain.exception.QueueAdvanceNotPossibleException;
import com.flowpay.routing.monitoring.distribution.domain.model.Interaction;
import com.flowpay.routing.monitoring.distribution.domain.port.in.AdvanceQueue;
import com.flowpay.routing.monitoring.distribution.domain.port.in.EndInteraction;
import com.flowpay.routing.monitoring.distribution.domain.port.out.InteractionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Frees one slot on a team so the queue moves forward. It ends the team's oldest ongoing
 * interaction and reuses {@link EndInteraction}, which is exactly the path that frees the
 * agent and pulls the next waiting customer into service.
 */
@Service
public class AdvanceQueueService implements AdvanceQueue {

    private final InteractionRepository interactions;
    private final EndInteraction endInteraction;

    public AdvanceQueueService(InteractionRepository interactions, EndInteraction endInteraction) {
        this.interactions = interactions;
        this.endInteraction = endInteraction;
    }

    @Override
    @Transactional
    public void handle(UUID teamId) {
        Interaction inService = interactions.findOldestInServiceByTeam(teamId)
                .orElseThrow(() -> new QueueAdvanceNotPossibleException(teamId));

        // Ending it frees the agent's slot, which pulls the next person from the queue.
        endInteraction.handle(inService.id());
    }
}
