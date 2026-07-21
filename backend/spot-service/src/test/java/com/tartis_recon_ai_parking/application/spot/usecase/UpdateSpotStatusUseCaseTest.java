package com.tartis_recon_ai_parking.application.spot.usecase;

import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.ParkingCapacity;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import com.tartis_recon_ai_parking.domain.spot.exception.InvalidSpotException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdateSpotStatusUseCaseTest {

    private SpotPersistence spotPersistence;
    private UpdateSpotStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        spotPersistence = mock(SpotPersistence.class);
        useCase = new UpdateSpotStatusUseCase(spotPersistence);
    }

    @Test
    void permiteOcuparSiHayCapacidadDisponible() {
        UUID id = UUID.randomUUID();
        Spot spot = new Spot(id, VehicleType.CAR, 12);
        int maxCar = ParkingCapacity.getMaxCapacityFor(VehicleType.CAR);

        when(spotPersistence.findById(id)).thenReturn(Optional.of(spot));
        when(spotPersistence.countByTypeAndStatus(VehicleType.CAR, SpotStatus.OCCUPIED))
                .thenReturn((long) (maxCar - 1));
        when(spotPersistence.save(any(Spot.class))).thenAnswer(inv -> inv.getArgument(0));

        Spot result = useCase.execute(id, SpotStatus.OCCUPIED);

        assertEquals(SpotStatus.OCCUPIED, result.getStatus());
        verify(spotPersistence).save(spot);
    }

    @Test
    void rechazaOcuparSiSeAlcanzoLaCapacidadMaxima() {
        UUID id = UUID.randomUUID();
        Spot spot = new Spot(id, VehicleType.CAR, 12);
        int maxCar = ParkingCapacity.getMaxCapacityFor(VehicleType.CAR);

        when(spotPersistence.findById(id)).thenReturn(Optional.of(spot));
        when(spotPersistence.countByTypeAndStatus(VehicleType.CAR, SpotStatus.OCCUPIED))
                .thenReturn((long) maxCar);

        assertThrows(InvalidSpotException.class, () -> useCase.execute(id, SpotStatus.OCCUPIED));

        verify(spotPersistence, never()).save(any());
    }

    @Test
    void laCapacidadSeComprueboaSoloParaElTipoDeLaPlazaAfectada() {
        UUID id = UUID.randomUUID();
        Spot spot = new Spot(id, VehicleType.CAR, 12);
        int maxCar = ParkingCapacity.getMaxCapacityFor(VehicleType.CAR);

        when(spotPersistence.findById(id)).thenReturn(Optional.of(spot));
        when(spotPersistence.countByTypeAndStatus(VehicleType.CAR, SpotStatus.OCCUPIED))
                .thenReturn((long) (maxCar - 1));
        when(spotPersistence.save(any(Spot.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> useCase.execute(id, SpotStatus.OCCUPIED));

        verify(spotPersistence, never()).countByTypeAndStatus(eq(VehicleType.MOTORBIKE), any());
    }

    @Test
    void liberaUnaPlazaOcupada() {
        UUID id = UUID.randomUUID();
        Spot spot = new Spot(id, VehicleType.CAR, 12, SpotStatus.OCCUPIED);

        when(spotPersistence.findById(id)).thenReturn(Optional.of(spot));
        when(spotPersistence.save(any(Spot.class))).thenAnswer(inv -> inv.getArgument(0));

        Spot result = useCase.execute(id, SpotStatus.AVAILABLE);

        assertEquals(SpotStatus.AVAILABLE, result.getStatus());
        verify(spotPersistence, never()).countByTypeAndStatus(any(), any());
    }

    @Test
    void lanzaSpotNotFoundExceptionSiElIdNoExiste() {
        UUID id = UUID.randomUUID();
        when(spotPersistence.findById(id)).thenReturn(Optional.empty());

        assertThrows(
            com.tartis_recon_ai_parking.domain.spot.exception.SpotNotFoundException.class,
            () -> useCase.execute(id, SpotStatus.OCCUPIED)
        );
    }
}