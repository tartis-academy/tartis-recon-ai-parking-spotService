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

import com.tartis_recon_ai_parking.application.spot.dto.SpotAvailabilityDTO;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;

@ExtendWith(MockitoExtension.class)
@DisplayName("AvailableSpotUseCase Tests")
class AvailableSpotUseCaseTest {

    @Mock
    private SpotPersistence spotPersistence;

    @InjectMocks
    private AvailableSpotUseCase availableSpotUseCase;

    @Test
    @DisplayName("Debe reportar disponible y los contadores si hay plazas libres del tipo solicitado")
    void execute_ShouldReportAvailable_WhenSpotsAreAvailable() {
        when(spotPersistence.countByTypeAndStatus(VehicleType.CAR, SpotStatus.AVAILABLE)).thenReturn(7L);
        when(spotPersistence.countByType(VehicleType.CAR)).thenReturn(20L);

        SpotAvailabilityDTO result = availableSpotUseCase.execute(VehicleType.CAR);

        assertThat(result.getType()).isEqualTo(VehicleType.CAR);
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.getAvailableCount()).isEqualTo(7L);
        assertThat(result.getTotalCount()).isEqualTo(20L);
    }

    @Test
    @DisplayName("Debe reportar no disponible si no queda ninguna plaza libre del tipo solicitado")
    void execute_ShouldReportUnavailable_WhenNoSpotsAreAvailable() {
        when(spotPersistence.countByTypeAndStatus(VehicleType.MOTORBIKE, SpotStatus.AVAILABLE)).thenReturn(0L);
        when(spotPersistence.countByType(VehicleType.MOTORBIKE)).thenReturn(5L);

        SpotAvailabilityDTO result = availableSpotUseCase.execute(VehicleType.MOTORBIKE);

        assertThat(result.isAvailable()).isFalse();
        assertThat(result.getAvailableCount()).isZero();
        assertThat(result.getTotalCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("RN-10: las plazas en mantenimiento no se cuentan como disponibles")
    void execute_ShouldNotCountUnavailableSpots() {
        // El parking tiene 4 plazas del tipo, todas fuera de servicio: total 4, disponibles 0.
        when(spotPersistence.countByTypeAndStatus(VehicleType.CAR_PMR, SpotStatus.AVAILABLE)).thenReturn(0L);
        when(spotPersistence.countByType(VehicleType.CAR_PMR)).thenReturn(4L);

        SpotAvailabilityDTO result = availableSpotUseCase.execute(VehicleType.CAR_PMR);

        assertThat(result.isAvailable()).isFalse();
        assertThat(result.getTotalCount()).isEqualTo(4L);
        verify(spotPersistence).countByTypeAndStatus(VehicleType.CAR_PMR, SpotStatus.AVAILABLE);
    }
}
