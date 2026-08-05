package com.tartis_recon_ai_parking.infrastructure.spot.adapter.output.eventpublisher;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tartis_recon_ai_parking.application.spot.dto.SpotStatusChangedEvent;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotEventPublisher;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;

@ExtendWith(MockitoExtension.class)
@DisplayName("SpotStatusChangedEventRelay Tests")
class SpotStatusChangedEventRelayTest {

    @Mock
    private SpotEventPublisher eventPublisher;

    @InjectMocks
    private SpotStatusChangedEventRelay relay;

    private SpotStatusChangedEvent anEvent() {
        Spot spot = Spot.reconstruct(UUID.randomUUID(), VehicleType.CAR, SpotStatus.OCCUPIED);
        return SpotStatusChangedEvent.of(spot, Instant.now());
    }

    @Test
    @DisplayName("Debe reenviar el evento al SpotEventPublisher tras el commit")
    void onSpotStatusChanged_ShouldForwardEventToPublisher() {
        SpotStatusChangedEvent event = anEvent();

        relay.onSpotStatusChanged(event);

        verify(eventPublisher).publish(event);
    }

    @Test
    @DisplayName("Si el publisher falla, se registra pero no se propaga")
    void onSpotStatusChanged_ShouldSwallowPublishFailure() {
        SpotStatusChangedEvent event = anEvent();
        doThrow(new IllegalStateException("rabbitmq no disponible"))
                .when(eventPublisher).publish(any(SpotStatusChangedEvent.class));

        assertDoesNotThrow(() -> relay.onSpotStatusChanged(event));

        verify(eventPublisher).publish(event);
    }
}
