package com.tartis_recon_ai_parking.infrastructure.spot.adapter.output.persistence;


import com.tartis_recon_ai_parking.domain.spot.VehicleType;

import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


import org.springframework.stereotype.Component;

import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.Spot;



@Component
public class SpotPersistenceAdapter implements SpotPersistence {

    private final SpotRepository repository;
    private final SpotPersistenceMapper mapper;

    public SpotPersistenceAdapter(SpotRepository repository, SpotPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

      @Override
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
    @Override
public long countByTypeAndStatus(VehicleType type, SpotStatus status) {
return repository.countByTypeAndStatus(type, status);
}

    @Override
    public long countByType(VehicleType type) {
        return repository.countByType(type);
    }

    // Sin @Transactional: la frontera vive en OccupySpotUseCase.execute(), que
    // es quien mantiene abierto el contexto de persistencia hasta el commit.
    // De eso depende el dirty checking de mas abajo.
    @Override
    public Optional<Spot> findAndOccupyAvailableSpot(VehicleType type) {
        return repository.findFirstAvailable(type)
                .map(entity -> {
                    Spot spot = mapper.toDomain(entity);
                    spot.occupy();

                    // Se aplica el cambio sobre la entidad GESTIONADA (la misma
                    // que findFirstAvailable dejo bloqueada con
                    // PESSIMISTIC_WRITE), no sobre una copia.
                    //
                    // Antes esto era repository.save(mapper.toEntity(spot)), y
                    // fallaba: el dominio Spot no lleva version, asi que
                    // toEntity() devuelve una instancia NUEVA con el mismo id y
                    // version=null. Spring Data mira la version en
                    // JpaMetamodelEntityInformation.isNew(), la ve nula, la trata
                    // como entidad nueva y hace persist() en vez de merge. Como
                    // ya hay una instancia gestionada con ese id, revienta con
                    //   NonUniqueObjectException: A different object with the
                    //   same identifier value was already associated with this
                    //   persistence context
                    // que el handler traduce a un 409 "Violacion de integridad de
                    // datos", y en el check-in se leia como "no hay plazas".
                    //
                    // Mutando la gestionada, el dirty checking emite el UPDATE al
                    // hacer commit, la version sube sola y el bloqueo pesimista
                    // sigue cubriendo la seccion critica.
                    entity.setStatus(spot.getStatus());
                    return mapper.toDomain(entity);
                });
    }

    // COUNT puro, sin bloqueo. La version anterior usaba findFirstAvailable
    // (SELECT ... FOR UPDATE), que Postgres rechaza en transaccion readOnly y
    // hacia fallar cada consulta de disponibilidad con un 500.
    @Override
    public boolean existsAvailableByType(VehicleType type) {
        return repository.countByTypeAndStatus(type, SpotStatus.AVAILABLE) > 0;
    }
}