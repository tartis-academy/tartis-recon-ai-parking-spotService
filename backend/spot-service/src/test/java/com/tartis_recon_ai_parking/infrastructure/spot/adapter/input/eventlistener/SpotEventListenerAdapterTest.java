package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.eventlistener;

import com.tartis_recon_ai_parking.application.spot.usecase.ReleaseSpotUseCase;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotOccupiedException;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.eventlistener.dto.StayClosedEvent;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.eventlistener.dto.StayClosedEventData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotEventListenerAdapterTest {

    @Mock
    private ReleaseSpotUseCase releaseSpotUseCase;

    @InjectMocks
    private SpotEventListenerAdapter adapter;

    private UUID spotId;

    @BeforeEach
    void setUp() {
        spotId = UUID.randomUUID();
    }

    @Test
    void handleStayClosedEvent_ValidEvent_ReleasesSpot() {
        // Arrange
        StayClosedEventData data = new StayClosedEventData(spotId);
        StayClosedEvent event = new StayClosedEvent(
                UUID.randomUUID(), "StayClosedEvent", "v1", Instant.now(), data
        );

        // Act
        adapter.handleStayClosedEvent(event);

        // Assert
        verify(releaseSpotUseCase, times(1)).execute(eq(spotId), any(Instant.class));
    }

    @Test
    void handleStayClosedEvent_NullEvent_ThrowsNullPointerException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> adapter.handleStayClosedEvent(null));
        verifyNoInteractions(releaseSpotUseCase);
    }

    @Test
    void handleStayClosedEvent_NullData_ThrowsNullPointerException() {
        // Arrange
        StayClosedEvent event = new StayClosedEvent(
                UUID.randomUUID(), "StayClosedEvent", "v1", Instant.now(), null
        );

        // Act & Assert
        assertThrows(NullPointerException.class, () -> adapter.handleStayClosedEvent(event));
        verifyNoInteractions(releaseSpotUseCase);
    }

    @Test
    void handleStayClosedEvent_NullSpotId_ThrowsNullPointerException() {
        // Arrange
        StayClosedEventData data = new StayClosedEventData(null);
        StayClosedEvent event = new StayClosedEvent(
                UUID.randomUUID(), "StayClosedEvent", "v1", Instant.now(), data
        );

        // Act & Assert
        assertThrows(NullPointerException.class, () -> adapter.handleStayClosedEvent(event));
        verifyNoInteractions(releaseSpotUseCase);
    }

    @Test
    void handleStayClosedEvent_Idempotency_IgnoresSpotNotOccupiedException() {
        // Arrange
        StayClosedEventData data = new StayClosedEventData(spotId);
        StayClosedEvent event = new StayClosedEvent(
                UUID.randomUUID(), "StayClosedEvent", "v1", Instant.now(), data
        );

        // Simula que la plaza ya está liberada y lanza la excepción del dominio
        doThrow(new SpotNotOccupiedException("Ya está disponible")).when(releaseSpotUseCase).execute(eq(spotId), any(Instant.class));

        // Act
        // No debe lanzar excepción hacia arriba (se captura para idempotencia)
        adapter.handleStayClosedEvent(event);

        // Assert
        verify(releaseSpotUseCase, times(1)).execute(eq(spotId), any(Instant.class));
    }

    @Test
    void handleStayClosedEvent_OtherException_RethrowsException() {
        // Arrange
        StayClosedEventData data = new StayClosedEventData(spotId);
        StayClosedEvent event = new StayClosedEvent(
                UUID.randomUUID(), "StayClosedEvent", "v1", Instant.now(), data
        );

        // Simula otro error inesperado (ej. BD caída)
        doThrow(new RuntimeException("Database error")).when(releaseSpotUseCase).execute(eq(spotId), any(Instant.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> adapter.handleStayClosedEvent(event));
        verify(releaseSpotUseCase, times(1)).execute(eq(spotId), any(Instant.class));
    }
}
