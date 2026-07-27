package com.tartis_recon_ai_parking.application.spot.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import com.tartis_recon_ai_parking.domain.spot.exception.NoAvailableSpotException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OccupySpotUseCase Tests")
class OccupySpotUseCaseTest {

    @Mock
    private SpotPersistence spotPersistence;

    @InjectMocks
    private OccupySpotUseCase occupySpotUseCase;

    @Test
    @DisplayName("Debe ocupar la plaza si el puerto de persistencia encuentra una disponible")
    void execute_WhenSpotAvailable_ShouldReturnOccupiedSpot() {
        // QUE HACE:
        // - Simula la obtención exitosa de una plaza disponible desde el repositorio, devolviendola con estado ocupado.
        // - Ejecuta el método execute.
        UUID spotId = UUID.randomUUID();
        Spot occupiedSpot = Spot.reconstruct(spotId, VehicleType.CAR, SpotStatus.OCCUPIED);

        when(spotPersistence.findAndOccupyAvailableSpot(VehicleType.CAR))
                .thenReturn(Optional.of(occupiedSpot));

        Spot result = occupySpotUseCase.execute(VehicleType.CAR);

        // QUE DEBERIA HACER:
        // Debe retornar la plaza en estado OCCUPIED.
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(SpotStatus.OCCUPIED);
    }

    @Test
    @DisplayName("Debe lanzar NoAvailableSpotException si no hay plazas libres")
    void execute_WhenNoSpotAvailable_ShouldThrowException() {
        // QUE HACE:
        // - Simula la búsqueda en persistencia devolviendo vacío (sin plazas disponibles).
        // - Ejecuta el método execute.
        when(spotPersistence.findAndOccupyAvailableSpot(VehicleType.CAR))
                .thenReturn(Optional.empty());

        // QUE DEBERIA HACER:
        // Debe lanzar una excepcion indicando que no hay plazas.
        assertThatThrownBy(() -> occupySpotUseCase.execute(VehicleType.CAR))
                .isInstanceOf(NoAvailableSpotException.class);
    }
}