package com.tartis_recon_ai_parking.application.spot.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;

@ExtendWith(MockitoExtension.class)
@DisplayName("AvailableSpotUseCase Tests")
class AvailableSpotUseCaseTest {

    @Mock
    private SpotPersistence spotPersistence;

    @InjectMocks
    private AvailableSpotUseCase availableSpotUseCase;

    @Test
    @DisplayName("Debe retornar true si existen plazas disponibles del tipo solicitado")
    void execute_ShouldReturnTrue_WhenSpotsAreAvailable() {
        // QUE HACE:
        // - Simula que la persistencia encuentra plazas disponibles para el tipo dado.
        when(spotPersistence.existsAvailableByType(VehicleType.CAR)).thenReturn(true);

        boolean result = availableSpotUseCase.execute(VehicleType.CAR);

        // QUE DEBERIA HACER:
        // Debe retornar true delegando en el puerto de persistencia.
        assertThat(result).isTrue();
        verify(spotPersistence).existsAvailableByType(VehicleType.CAR);
    }

    @Test
    @DisplayName("Debe retornar false si no existen plazas disponibles del tipo solicitado")
    void execute_ShouldReturnFalse_WhenNoSpotsAreAvailable() {
        // QUE HACE:
        // - Simula que la persistencia no encuentra plazas disponibles para el tipo dado.
        when(spotPersistence.existsAvailableByType(VehicleType.MOTORBIKE)).thenReturn(false);

        boolean result = availableSpotUseCase.execute(VehicleType.MOTORBIKE);

        // QUE DEBERIA HACER:
        // Debe retornar false delegando en el puerto de persistencia.
        assertThat(result).isFalse();
        verify(spotPersistence).existsAvailableByType(VehicleType.MOTORBIKE);
    }
}
