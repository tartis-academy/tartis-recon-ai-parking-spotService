package com.tartis_recon_ai_parking.infrastructure.spot.adapter.output.persistence;

import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Tests del adaptador contra un EntityManager REAL.
 *
 * SpotPersistenceAdapterTest mockea SpotRepository, asi que nunca ejecuta
 * SimpleJpaRepository.save() ni su decision persist()/merge(). Ese es el hueco
 * por el que se colo el fallo de occupy y por el que estuvo a punto de colarse
 * el mismo fallo en release, update y updateStatus al anadir @Version.
 *
 * Estos tests si lo cubren: son los que se ponen en rojo si save() vuelve a
 * remapear el dominio a una entidad nueva en vez de mutar la gestionada.
 */
@DataJpaTest
@DisplayName("SpotPersistenceAdapter contra JPA real")
class SpotPersistenceAdapterJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SpotRepository spotRepository;

    private SpotPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        // El slice de @DataJpaTest no registra los @Mapper de MapStruct, asi que
        // se instancia la implementacion generada directamente.
        adapter = new SpotPersistenceAdapter(spotRepository, new SpotPersistenceMapperImpl());
    }

    private UUID givenSpotEnBd(VehicleType type, SpotStatus status) {
        SpotEntity entity = new SpotEntity();
        entity.setId(UUID.randomUUID());
        entity.setType(type);
        entity.setStatus(status);
        entityManager.persistAndFlush(entity);
        entityManager.clear();
        return entity.getId();
    }

    @Test
    @DisplayName("save() sobre una plaza ya cargada debe actualizarla, no tratarla como nueva")
    void save_ShouldUpdateExistingSpot_NotTreatItAsNew() {
        // QUE HACE:
        // - Reproduce la secuencia de ReleaseSpotUseCase: findById deja la entidad
        //   GESTIONADA en el contexto, el dominio cambia de estado y se guarda.
        UUID id = givenSpotEnBd(VehicleType.CAR, SpotStatus.OCCUPIED);

        Spot spot = adapter.findById(id).orElseThrow();
        spot.release();

        // QUE DEBERIA HACER:
        // Debe aplicar el UPDATE sin reventar. Si save() remapea el dominio a una
        // SpotEntity nueva, esta sale con version=null, isNew() la da por nueva y
        // el persist() resultante choca con la instancia ya gestionada:
        //   NonUniqueObjectException: A different object with the same identifier
        //   value was already associated with the session
        assertThatCode(() -> adapter.save(spot)).doesNotThrowAnyException();
        entityManager.flush();
        entityManager.clear();

        assertThat(adapter.findById(id).orElseThrow().getStatus()).isEqualTo(SpotStatus.AVAILABLE);
    }

    @Test
    @DisplayName("save() sobre una plaza ya cargada debe persistir tambien el cambio de tipo")
    void save_ShouldPersistTypeChange_OnExistingSpot() {
        // QUE HACE:
        // - Reproduce la secuencia de UpdateSpotUseCase, que ademas del estado
        //   toca el tipo de vehiculo.
        UUID id = givenSpotEnBd(VehicleType.CAR, SpotStatus.UNAVAILABLE);

        Spot existente = adapter.findById(id).orElseThrow();
        Spot cambiada = existente.changeTypeTo(VehicleType.CAR_PMR);

        adapter.save(cambiada);
        entityManager.flush();
        entityManager.clear();

        // QUE DEBERIA HACER:
        // El nuevo tipo debe llegar a BD y el estado de mantenimiento quedar intacto.
        Spot recargada = adapter.findById(id).orElseThrow();
        assertThat(recargada.getType()).isEqualTo(VehicleType.CAR_PMR);
        assertThat(recargada.getStatus()).isEqualTo(SpotStatus.UNAVAILABLE);
    }

    @Test
    @DisplayName("save() de una plaza nueva debe insertarla e inicializar la version")
    void save_ShouldInsertNewSpot() {
        // QUE HACE:
        // - Reproduce la secuencia de CreateSpotUseCase: la plaza no existe en BD,
        //   asi que aqui persist() si es lo correcto.
        Spot nueva = Spot.create(VehicleType.MOTORBIKE);

        Spot guardada = adapter.save(nueva);
        entityManager.flush();
        entityManager.clear();

        // QUE DEBERIA HACER:
        // Debe quedar insertada y con la version que Hibernate asigna al INSERT,
        // que es lo que hace que el siguiente save() la reconozca como existente.
        assertThat(adapter.findById(guardada.getId())).isPresent();
        assertThat(entityManager.find(SpotEntity.class, guardada.getId()).getVersion()).isNotNull();
    }

    @Test
    @DisplayName("findAndOccupyAvailableSpot debe dejar la plaza OCCUPIED en BD")
    void findAndOccupyAvailableSpot_ShouldPersistOccupation() {
        // QUE HACE:
        // - Cubre la ruta de ocupacion sobre JPA real, que hasta ahora solo se
        //   probaba con el repositorio mockeado.
        UUID id = givenSpotEnBd(VehicleType.CAR, SpotStatus.AVAILABLE);

        assertThat(adapter.findAndOccupyAvailableSpot(VehicleType.CAR)).isPresent();
        entityManager.flush();
        entityManager.clear();

        // QUE DEBERIA HACER:
        // El dirty checking sobre la entidad gestionada debe emitir el UPDATE.
        assertThat(adapter.findById(id).orElseThrow().getStatus()).isEqualTo(SpotStatus.OCCUPIED);
    }
}
