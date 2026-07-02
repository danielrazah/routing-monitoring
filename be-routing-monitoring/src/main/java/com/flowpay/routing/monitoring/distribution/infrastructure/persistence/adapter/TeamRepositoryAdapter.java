package com.flowpay.routing.monitoring.distribution.infrastructure.persistence.adapter;

import com.flowpay.routing.monitoring.distribution.domain.model.Team;
import com.flowpay.routing.monitoring.distribution.domain.port.out.TeamRepository;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.mapper.TeamMapper;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository.TeamJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TeamRepositoryAdapter implements TeamRepository {

    private final TeamJpaRepository jpa;

    public TeamRepositoryAdapter(TeamJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Team> findByName(String name) {
        return jpa.findByName(name).map(TeamMapper::toDomain);
    }
}
