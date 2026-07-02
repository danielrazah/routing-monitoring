package com.flowpay.routing.monitoring.distribution.domain.port.out;

import java.util.Optional;
import java.util.UUID;

/**
 * The team's persistent waiting line.
 *
 * Kept in the database so it survives restarts and, crucially, so many workers can
 * drain it at the same time without two of them grabbing the same customer. The adapter
 * enforces that with SELECT ... FOR UPDATE SKIP LOCKED.
 */
public interface WaitingQueuePort {

    /** Put a customer at the back of the team's line. */
    void enqueue(UUID teamId, UUID interactionId);

    /** Take the next waiting customer for a team, or empty if the line is empty. */
    Optional<UUID> pollNext(UUID teamId);
}
