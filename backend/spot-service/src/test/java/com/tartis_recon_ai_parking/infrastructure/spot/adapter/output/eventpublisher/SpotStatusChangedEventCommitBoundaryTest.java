package com.tartis_recon_ai_parking.infrastructure.spot.adapter.output.eventpublisher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import com.tartis_recon_ai_parking.application.spot.dto.SpotStatusChangedEvent;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotEventPublisher;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.application.spot.usecase.OccupySpotUseCase;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.eventlistener.SpotEventListenerAdapter;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.output.persistence.SpotRepository;

/**
 * Cubre la correccion de revision: el evento se publica dentro del
 * @Transactional del use case, antes del commit real. Aqui se comprueba
 * con un contexto Spring real (transaccion + SpotStatusChangedEventRelay)
 * que un rollback evita la publicacion, y que un commit la dispara.
 */
@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
@Transactional
@DisplayName("Publicacion de SpotStatusChangedEvent ligada al commit de la transaccion")
class SpotStatusChangedEventCommitBoundaryTest {

    @MockitoBean
    private SpotEventPublisher eventPublisher;

    @MockitoBean
    private SpotEventListenerAdapter spotEventListenerAdapter;

    @Autowired
    private OccupySpotUseCase occupySpotUseCase;

    @Autowired
    private SpotPersistence spotPersistence;

    @Autowired
    private SpotRepository spotRepository;

    @AfterEach
    void cleanUp() {
        // shouldPublishEvent_WhenTransactionCommits hace un commit real sobre la
        // H2 compartida (DB_CLOSE_DELAY=-1): sin este borrado, la plaza queda
        // contaminando el resto de tests que corran en la misma JVM.
        spotRepository.deleteAll();
    }

    @Test
    @DisplayName("No debe publicarse ningun evento si la transaccion hace rollback")
    void shouldNotPublishEvent_WhenTransactionRollsBack() {
        spotPersistence.save(Spot.create(VehicleType.CAR));

        occupySpotUseCase.execute(VehicleType.CAR);

        TestTransaction.flagForRollback();
        TestTransaction.end();

        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("Debe publicarse el evento una vez la transaccion hace commit")
    void shouldPublishEvent_WhenTransactionCommits() {
        spotPersistence.save(Spot.create(VehicleType.CAR));

        occupySpotUseCase.execute(VehicleType.CAR);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        verify(eventPublisher).publish(any(SpotStatusChangedEvent.class));
    }
}
