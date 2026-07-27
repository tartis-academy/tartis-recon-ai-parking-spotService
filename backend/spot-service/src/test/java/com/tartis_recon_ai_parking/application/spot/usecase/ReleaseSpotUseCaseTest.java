package com.tartis_recon_ai_parking.application.spot.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

import com.tartis_recon_ai_parking.application.spot.dto.SpotDTO;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotFoundException;

@ExtendWith(MockitoExtension.class)
public class ReleaseSpotUseCaseTest {

    @Mock
    private SpotPersistence spotPersistence;

    private ReleaseSpotUseCase releaseSpotUseCase;

    @BeforeEach
    void setUp() {
        releaseSpotUseCase = new ReleaseSpotUseCase(spotPersistence);
    }

    @Test
    @DisplayName("Debería liberar la plaza correctamente si existe")
    void execute_ShouldReleaseSpot_WhenSpotExists() {
        // QUE HACE:
        // - Busca la plaza por ID y si existe cambia su estado a AVAILABLE.
        // - Guarda la plaza mediante SpotPersistence.
        UUID spotId = UUID.randomUUID();
        Spot existingSpot = Spot.reconstruct(spotId, VehicleType.CAR, SpotStatus.OCCUPIED);
        
        when(spotPersistence.findById(spotId)).thenReturn(Optional.of(existingSpot));
        when(spotPersistence.save(any(Spot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SpotDTO result = releaseSpotUseCase.execute(spotId);

        // QUE DEBERIA HACER:
        // Debe retornar el SpotDTO con el nuevo estado AVAILABLE y verificar la llamada a save.
        assertNotNull(result);
        assertEquals(spotId, result.getId());
        assertEquals(SpotStatus.AVAILABLE, result.getStatus());
        verify(spotPersistence).save(existingSpot);
    }

    @Test
    @DisplayName("Debería lanzar SpotNotFoundException si la plaza no existe")
    void execute_ShouldThrowSpotNotFoundException_WhenSpotDoesNotExist() {
        // QUE HACE:
        // - Configura la persistencia para devolver Optional vacio (plaza no encontrada).
        // - Ejecuta la liberacion de la plaza.
        UUID spotId = UUID.randomUUID();
        
        when(spotPersistence.findById(spotId)).thenReturn(Optional.empty());

        // QUE DEBERIA HACER:
        // Debe lanzar SpotNotFoundException y no se invoca el metodo save().
        assertThrows(SpotNotFoundException.class, () -> releaseSpotUseCase.execute(spotId));
    }
}
