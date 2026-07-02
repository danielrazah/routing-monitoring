package com.flowpay.routing.monitoring.distribution.domain.port.in;

import java.util.UUID;

/**
 * Use case: an ongoing interaction is finished. Closing it frees the agent's slot,
 * which immediately pulls the next waiting customer (if any) into service.
 */
public interface EndInteraction {

    void handle(UUID interactionId);
}
