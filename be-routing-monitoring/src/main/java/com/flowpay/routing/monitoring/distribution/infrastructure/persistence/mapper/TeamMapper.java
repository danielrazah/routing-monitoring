package com.flowpay.routing.monitoring.distribution.infrastructure.persistence.mapper;

import com.flowpay.routing.monitoring.distribution.domain.model.Team;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.entity.TeamJpaEntity;

public final class TeamMapper {

    private TeamMapper() {
    }

    public static Team toDomain(TeamJpaEntity entity) {
        return new Team(entity.getId(), entity.getName());
    }
}
