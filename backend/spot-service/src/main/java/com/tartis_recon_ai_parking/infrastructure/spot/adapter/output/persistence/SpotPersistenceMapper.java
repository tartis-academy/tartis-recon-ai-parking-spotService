package com.tartis_recon_ai_parking.infrastructure.spot.adapter.output.persistence;

import com.tartis_recon_ai_parking.domain.spot.Spot;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;

// Mapper de infraestructura utilizado para traducir entre el modelo de Dominio y el modelo de Base de Datos.
@Mapper(componentModel = "spring")
public interface SpotPersistenceMapper {

    SpotEntity toEntity(Spot spot);

    Spot toDomain(SpotEntity entity);

    @ObjectFactory
    default Spot createSpot(SpotEntity entity) {
        return Spot.reconstruct(entity.getId(), entity.getType(), entity.getStatus());
    }
}