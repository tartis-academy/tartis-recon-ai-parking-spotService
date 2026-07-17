package com.tartis_recon_ai_parking.infrastructure.spot.adapter.output.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.Spot;

import jakarta.transaction.Transactional;


@Component
public class SpotPersistenceAdapter implements SpotPersistence {

    private final SpotRepository repository;
    private final SpotPersistenceMapper mapper;

    public SpotPersistenceAdapter(SpotRepository repository, SpotPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Spot save(Spot spot) {
        SpotEntity saved = repository.save(mapper.toEntity(spot));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Spot> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Spot> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
}