package com.tartis_recon_ai_parking.application.spot.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotCannotBeBlockedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateSpotStatusUseCase Tests")
class UpdateSpotStatusUseCaseTest {

    @Mock
    private SpotPersistence spotPersistence;

    private UpdateSpotStatusUseCase updateSpotStatusUseCase;

    @BeforeEach
    void setUp() {
        updateSpotStatusUseCase = new UpdateSpotStatusUseCase(spotPersistence);
    }

    @Test
    @DisplayName("Debe bloquear la plaza al pasar de AVAILABLE a UNAVAILABLE")
    void execute_ShouldBlockSpot_WhenAvailableToUnavailable() {
        // QUE HACE:
        // - Simula una plaza DISPONIBLE y solicita el estado UNAVAILABLE.
        UUID spotId = UUID.randomUUID();
        Spot spot = Spot.reconstruct(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);

        when(spotPersistence.findById(spotId)).thenReturn(Optional.of(spot));
        when(spotPersistence.save(any(Spot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Spot result = updateSpotStatusUseCase.execute(spotId, SpotStatus.UNAVAILABLE);

        // QUE DEBERIA HACER:
        // Debe retornar la plaza en estado UNAVAILABLE y persistirla.
        assertThat(result.getStatus()).isEqualTo(SpotStatus.UNAVAILABLE);
        verify(spotPersistence).save(spot);
    }

    @Test
    @DisplayName("Debe desbloquear la plaza al pasar de UNAVAILABLE a AVAILABLE")
    void execute_ShouldUnblockSpot_WhenUnavailableToAvailable() {
        // QUE HACE:
        // - Simula una plaza EN MANTENIMIENTO y solicita el estado AVAILABLE.
        UUID spotId = UUID.randomUUID();
        Spot spot = Spot.reconstruct(spotId, VehicleType.CAR, SpotStatus.UNAVAILABLE);

        when(spotPersistence.findById(spotId)).thenReturn(Optional.of(spot));
        when(spotPersistence.save(any(Spot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Spot result = updateSpotStatusUseCase.execute(spotId, SpotStatus.AVAILABLE);

        // QUE DEBERIA HACER:
        // Debe retornar la plaza en estado AVAILABLE y persistirla.
        assertThat(result.getStatus()).isEqualTo(SpotStatus.AVAILABLE);
        verify(spotPersistence).save(spot);
    }

    @Test
    @DisplayName("Debe lanzar SpotCannotBeBlockedException si se solicita OCCUPIED por este endpoint")
    void execute_ShouldThrowConflict_WhenOccupiedIsRequested() {
        // QUE HACE:
        // - Simula una plaza DISPONIBLE y solicita el estado OCCUPIED, reservado a POST /spots/occupy.
        UUID spotId = UUID.randomUUID();
        Spot spot = Spot.reconstruct(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);

        when(spotPersistence.findById(spotId)).thenReturn(Optional.of(spot));

        // QUE DEBERIA HACER:
        // Debe lanzar la excepcion de conflicto sin persistir ningun cambio.
        assertThatThrownBy(() -> updateSpotStatusUseCase.execute(spotId, SpotStatus.OCCUPIED))
                .isInstanceOf(SpotCannotBeBlockedException.class);
        verify(spotPersistence, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar SpotCannotBeBlockedException si la plaza ya esta OCCUPIED")
    void execute_ShouldThrowConflict_WhenSpotIsOccupied() {
        // QUE HACE:
        // - Simula una plaza OCUPADA y solicita el estado UNAVAILABLE.
        UUID spotId = UUID.randomUUID();
        Spot spot = Spot.reconstruct(spotId, VehicleType.CAR, SpotStatus.OCCUPIED);

        when(spotPersistence.findById(spotId)).thenReturn(Optional.of(spot));

        // QUE DEBERIA HACER:
        // Debe lanzar la excepcion de conflicto (RN-10) sin persistir ningun cambio.
        assertThatThrownBy(() -> updateSpotStatusUseCase.execute(spotId, SpotStatus.UNAVAILABLE))
                .isInstanceOf(SpotCannotBeBlockedException.class);
        verify(spotPersistence, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar SpotCannotBeBlockedException si la plaza OCCUPIED se quiere pasar a AVAILABLE")
    void execute_ShouldThrowConflict_WhenOccupiedSpotIsReleasedThroughThisEndpoint() {
        // QUE HACE:
        // - Simula una plaza OCUPADA y solicita AVAILABLE, reservado a POST /spots/{id}/release.
        UUID spotId = UUID.randomUUID();
        Spot spot = Spot.reconstruct(spotId, VehicleType.CAR, SpotStatus.OCCUPIED);

        when(spotPersistence.findById(spotId)).thenReturn(Optional.of(spot));

        // QUE DEBERIA HACER:
        // Debe lanzar la excepcion de conflicto sin liberar la plaza.
        assertThatThrownBy(() -> updateSpotStatusUseCase.execute(spotId, SpotStatus.AVAILABLE))
                .isInstanceOf(SpotCannotBeBlockedException.class);
        verify(spotPersistence, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar SpotNotFoundException si la plaza no existe")
    void execute_ShouldThrowSpotNotFoundException_WhenSpotDoesNotExist() {
        // QUE HACE:
        // - Configura la persistencia para devolver Optional vacio.
        UUID spotId = UUID.randomUUID();

        when(spotPersistence.findById(spotId)).thenReturn(Optional.empty());

        // QUE DEBERIA HACER:
        // Debe lanzar SpotNotFoundException y no invocar save().
        assertThatThrownBy(() -> updateSpotStatusUseCase.execute(spotId, SpotStatus.UNAVAILABLE))
                .isInstanceOf(SpotNotFoundException.class);
        verify(spotPersistence, never()).save(any());
    }

    @Test
    @DisplayName("Debe ser idempotente si se solicita el estado que la plaza ya tiene")
    void execute_ShouldBeNoOp_WhenStatusIsUnchanged() {
        // QUE HACE:
        // - Simula una plaza DISPONIBLE y vuelve a solicitar AVAILABLE.
        UUID spotId = UUID.randomUUID();
        Spot spot = Spot.reconstruct(spotId, VehicleType.CAR, SpotStatus.AVAILABLE);

        when(spotPersistence.findById(spotId)).thenReturn(Optional.of(spot));
        when(spotPersistence.save(any(Spot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Spot result = updateSpotStatusUseCase.execute(spotId, SpotStatus.AVAILABLE);

        // QUE DEBERIA HACER:
        // Debe mantener el estado AVAILABLE sin error y seguir persistiendo la plaza.
        assertThat(result.getStatus()).isEqualTo(SpotStatus.AVAILABLE);
        verify(spotPersistence).save(spot);
    }
}
