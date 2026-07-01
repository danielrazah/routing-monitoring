package com.flowpay.routing.monitoring.distribution.domain.event;

import java.time.Instant;

/**
 * Something meaningful that happened while distributing work. The core raises these
 * so other parts (dashboard, logs) can react, without the core knowing who's listening.
 */
public sealed interface DomainEvent
        permits InteractionCreated, InteractionAssigned, InteractionQueued, InteractionEnded {

    Instant occurredAt();
}
