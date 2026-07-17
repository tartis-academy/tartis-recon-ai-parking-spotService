package com.tartis_recon_ai_parking.infrastructure.spot.adapter.output.persistence;



import com.tartis_recon_ai_parking.domain.spot.Spot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
 
// Mapper de infraestructura utilizado para traducir entre el modelo de Dominio y el modelo de Base de Datos.
@Mapper(componentModel = "spring")
public interface SpotPersistenceMapper {
 
    // Convierte un objeto del modelo de dominio (Spot) a una entidad tecnologica (SpotEntity).
    @Mapping(source = "id", target = "uniqueId")
    SpotEntity toEntity(Spot spot);
 
    // Convierte una entidad tecnologica (SpotEntity) recuperada de la base de datos de vuelta al modelo de dominio puro (Spot).
    @Mapping(source = "uniqueId", target = "id")
    Spot toDomain(SpotEntity entity);
}