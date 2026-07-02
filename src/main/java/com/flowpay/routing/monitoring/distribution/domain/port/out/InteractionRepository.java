package com.flowpay.routing.monitoring.distribution.domain.port.out;

import com.flowpay.routing.monitoring.distribution.domain.model.Interaction;

import java.util.Optional;
import java.util.UUID;

/** Stores and loads interactions. */
public interface InteractionRepository {

    Interaction save(Interaction interaction);

    Optional<Interaction> findById(UUID id);
}
