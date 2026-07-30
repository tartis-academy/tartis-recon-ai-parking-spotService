package com.tartis_recon_ai_parking.infrastructure.spot.adapter.output.persistence;

import com.tartis_recon_ai_parking.domain.spot.Spot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;

// Mapper de infraestructura utilizado para traducir entre el modelo de Dominio y el modelo de Base de Datos.
@Mapper(componentModel = "spring")
public interface SpotPersistenceMapper {

    SpotEntity toEntity(Spot spot);

    Spot toDomain(SpotEntity entity);

    /**
     * Vuelca el estado del dominio sobre una entidad YA GESTIONADA por el
     * contexto de persistencia, en lugar de construir una instancia nueva.
     *
     * Lo genera MapStruct a proposito: si manana Spot y SpotEntity ganan un
     * campo, este metodo lo incluye solo. Copiarlo a mano en el adaptador
     * significaria que el campo nuevo se pierde en silencio al actualizar una
     * plaza existente, con el codigo compilando y los tests en verde.
     *
     * - id se ignora: es la clave de la entidad gestionada y ya coincide, y
     *   reasignar el identificador de una instancia gestionada es justo lo que
     *   Hibernate prohibe ("identifier of an instance was altered").
     * - version no se toca: la lleva Hibernate y el dominio Spot no la conoce.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(@MappingTarget SpotEntity entity, Spot spot);

    @ObjectFactory
    default Spot createSpot(SpotEntity entity) {
        return Spot.reconstruct(entity.getId(), entity.getType(), entity.getStatus());
    }
}