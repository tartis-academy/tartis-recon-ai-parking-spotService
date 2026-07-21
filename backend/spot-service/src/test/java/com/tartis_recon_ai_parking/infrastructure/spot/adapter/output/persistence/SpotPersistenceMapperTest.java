package com.tartis_recon_ai_parking.infrastructure.spot.adapter.output.persistence;

import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SpotPersistenceMapperTest {

    // Utiliza el cargador de MapStruct Mappers en lugar de instanciar directamente
    // para evitar problemas de sincronizacion del compilador de la IDE.
    private final SpotPersistenceMapper mapper = Mappers.getMapper(SpotPersistenceMapper.class);

    @Test
    @DisplayName("Debe mapear un objeto de dominio Spot a una entidad SpotEntity de forma correcta")
    void shouldMapSpotToEntity() {
        // QUE HACE:
        // Instancia un objeto Spot de dominio completo con datos especificos, llama
        // al metodo toEntity del mapeador.
        UUID id = UUID.randomUUID();
        Spot spot = Spot.reconstruct(id, VehicleType.CAR, SpotStatus.AVAILABLE);

        SpotEntity entity = mapper.toEntity(spot);

        // QUE DEBERIA HACER:
        // Debe retornar un objeto SpotEntity no nulo y comprobar mediante aserciones
        // de AssertJ que cada campo de persistencia coincida exactamente con el de origen.
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getType()).isEqualTo(VehicleType.CAR);
        assertThat(entity.getStatus()).isEqualTo(SpotStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Debe retornar null al mapear un spot de dominio nulo a entidad")
    void shouldReturnNullWhenMappingNullSpot() {
        // QUE HACE:
        // Llama al mapper pasando un parametro nulo.
        SpotEntity entity = mapper.toEntity(null);
        
        // QUE DEBERIA HACER:
        // El mapper debe retornar null de forma segura y sin lanzar excepciones.
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("Debe mapear una entidad SpotEntity a un objeto de dominio Spot de forma correcta")
    void shouldMapEntityToSpot() {
        // QUE HACE:
        // Instancia y rellena un SpotEntity con datos de prueba especificos, llama
        // al metodo toDomain del mapeador.
        UUID id = UUID.randomUUID();
        SpotEntity entity = new SpotEntity();
        entity.setId(id);
        entity.setType(VehicleType.MOTORBIKE);
        entity.setStatus(SpotStatus.OCCUPIED);

        Spot spot = mapper.toDomain(entity);

        // QUE DEBERIA HACER:
        // Debe retornar un objeto de dominio Spot no nulo y comprobar mediante aserciones
        // que todos los campos del dominio se correspondan de forma exacta con los de la entidad.
        assertThat(spot).isNotNull();
        assertThat(spot.getId()).isEqualTo(id);
        assertThat(spot.getType()).isEqualTo(VehicleType.MOTORBIKE);
        assertThat(spot.getStatus()).isEqualTo(SpotStatus.OCCUPIED);
    }

    @Test
    @DisplayName("Debe retornar null al mapear una entidad nula a dominio")
    void shouldReturnNullWhenMappingNullEntity() {
        // QUE HACE:
        // Llama al mapper pasando una entidad nula.
        Spot spot = mapper.toDomain(null);
        
        // QUE DEBERIA HACER:
        // El mapper debe retornar null de forma segura y sin lanzar excepciones.
        assertThat(spot).isNull();
    }
}
