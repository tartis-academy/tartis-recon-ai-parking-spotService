package com.tartis_recon_ai_parking.infrastructure.spot.adapter.output.persistence;


import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// @DataJpaTest: Configura un entorno de pruebas enfocado únicamente en la capa JPA.
// Levanta una base de datos embebida (H2) y autoconfigura los repositorios y EntityManager.
@DataJpaTest
class SpotRepositoryTest {

    // TestEntityManager: Herramienta de Spring Boot para pruebas de persistencia que permite 
    // realizar operaciones basicas (persist, flush, etc.) en la BD de pruebas aislando la fase de preparacion 
    @Autowired
    private TestEntityManager entityManager;

    // Repositorio bajo prueba
    @Autowired
    private SpotRepository spotRepository;

    @Test
    @DisplayName("Debe contar correctamente el numero de plazas disponibles por tipo obviando otros tipos y estados")
    void shouldCountByTypeAndStatusSuccessfully() {
        // QUE HACE:
        // - Instancia y rellena multiples SpotEntity con diferentes combinaciones de tipo y estado.
        // - (2 CAR disponibles, 1 CAR ocupado, 1 MOTORBIKE disponible).
        // - Persiste las entidades en la BD usando TestEntityManager.
        // - Ejecuta la consulta countByTypeAndStatus para recuperar las CAR disponibles.
        SpotEntity entity1 = new SpotEntity();
        entity1.setId(UUID.randomUUID());
        entity1.setType(VehicleType.CAR);
        entity1.setStatus(SpotStatus.AVAILABLE);

        SpotEntity entity2 = new SpotEntity();
        entity2.setId(UUID.randomUUID());
        entity2.setType(VehicleType.CAR);
        entity2.setStatus(SpotStatus.AVAILABLE);

        SpotEntity entity3 = new SpotEntity();
        entity3.setId(UUID.randomUUID());
        entity3.setType(VehicleType.CAR);
        entity3.setStatus(SpotStatus.OCCUPIED);

        SpotEntity entity4 = new SpotEntity();
        entity4.setId(UUID.randomUUID());
        entity4.setType(VehicleType.MOTORBIKE);
        entity4.setStatus(SpotStatus.AVAILABLE);

        entityManager.persist(entity1);
        entityManager.persist(entity2);
        entityManager.persist(entity3);
        entityManager.persist(entity4);
        entityManager.flush();

        long count = spotRepository.countByTypeAndStatus(VehicleType.CAR, SpotStatus.AVAILABLE);

        // QUE DEBERIA HACER:
        // Debe retornar exactamente 2, ignorando la plaza ocupada y la plaza de tipo MOTORBIKE.
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("Debe retornar 0 al contar plazas por tipo y estado si no existen coincidencias exactas")
    void shouldReturnZeroWhenCountingByTypeAndStatusNotFound() {
        // QUE HACE:
        // Llama directamente a countByTypeAndStatus con una BD vacia o sin coincidencias.
        long count = spotRepository.countByTypeAndStatus(VehicleType.CAR, SpotStatus.OCCUPIED);

        // QUE DEBERIA HACER:
        // Debe retornar 0 al no encontrar ninguna plaza que cumpla la condicion.
        assertThat(count).isEqualTo(0L);
    }

    @Test
    @DisplayName("Debe guardar una plaza en la base de datos y permitir recuperarla por ID")
    void shouldSaveAndLoadSpotEntity() {
        // QUE HACE:
        // - Crea un SpotEntity de prueba.
        // - Llama al metodo save del repositorio para guardarlo.
        // - Recupera la entidad utilizando findById.
        UUID id = UUID.randomUUID();
        SpotEntity entity = new SpotEntity();
        entity.setId(id);
        entity.setType(VehicleType.CAR);
        entity.setStatus(SpotStatus.AVAILABLE);

        SpotEntity savedEntity = spotRepository.save(entity);

        // QUE DEBERIA HACER:
        // La entidad guardada debe ser no nula y poder recuperarse por ID.
        // Sus atributos persistidos deben coincidir.
        assertThat(savedEntity).isNotNull();
        
        Optional<SpotEntity> loadedEntityOpt = spotRepository.findById(id);
        assertThat(loadedEntityOpt).isPresent();
        assertThat(loadedEntityOpt.get().getType()).isEqualTo(VehicleType.CAR);
        assertThat(loadedEntityOpt.get().getStatus()).isEqualTo(SpotStatus.AVAILABLE);
    }
}
