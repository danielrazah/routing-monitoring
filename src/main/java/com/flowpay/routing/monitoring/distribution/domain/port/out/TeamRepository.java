package com.flowpay.routing.monitoring.distribution.domain.port.out;

import com.flowpay.routing.monitoring.distribution.domain.model.Team;

import java.util.Optional;

/** Looks teams up by the name the routing strategies produce. */
public interface TeamRepository {

    Optional<Team> findByName(String name);
}
