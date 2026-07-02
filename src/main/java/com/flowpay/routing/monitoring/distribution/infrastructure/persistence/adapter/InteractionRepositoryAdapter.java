package com.flowpay.routing.monitoring.distribution.infrastructure.persistence.adapter;

import com.flowpay.routing.monitoring.distribution.domain.model.Interaction;
import com.flowpay.routing.monitoring.distribution.domain.port.out.InteractionRepository;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.mapper.InteractionMapper;
import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository.InteractionJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class InteractionRepositoryAdapter implements InteractionRepository {

    private final InteractionJpaRepository jpa;

    public InteractionRepositoryAdapter(InteractionJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Interaction save(Interaction interaction) {
        jpa.save(InteractionMapper.toEntity(interaction));
        return interaction;
    }

    @Override
    public Optional<Interaction> findById(UUID id) {
        return jpa.findById(id).map(InteractionMapper::toDomain);
    }

    @Override
    public Optional<Interaction> findOldestInServiceByTeam(UUID teamId) {
        return jpa.findOldestInServiceByTeam(teamId).map(InteractionMapper::toDomain);
    }
}
