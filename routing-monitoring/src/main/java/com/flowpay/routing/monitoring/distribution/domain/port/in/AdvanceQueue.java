package com.flowpay.routing.monitoring.distribution.domain.port.in;

import java.util.UUID;

/**
 * Use case: manually free one slot on a team so the next customer in line gets served.
 * It ends the team's longest-running ongoing interaction, which opens a slot and pulls
 * the next waiting customer into service. Handy for driving the queue during testing.
 */
public interface AdvanceQueue {

    void handle(UUID teamId);
}
