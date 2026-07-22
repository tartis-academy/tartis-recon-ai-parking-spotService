package com.tartis_recon_ai_parking.application.spot.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tartis_recon_ai_parking.application.spot.dto.SpotCreateDTO;
import com.tartis_recon_ai_parking.application.spot.dto.SpotDTO;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para UpdateSpotUseCase.
 * Requiere las mismas dependencias de Mockito indicadas en CreateSpotUseCaseTests.
 */
@ExtendWith(MockitoExtension.class)
public class UpdateSpotUseCaseTests {

    @Mock
    private SpotPersistence spotPersistence;

    private UpdateSpotUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateSpotUseCase(spotPersistence);
    }

    // Test: execute(id, updateDTO) cuando la plaza existe debe reconstruirla
    // manteniendo el mismo id y el mismo estado (status) que tenía, mientras
    // que el tipo de vehículo se actualiza con el valor del DTO recibido.
    // Resultado esperado: se guarda una plaza con el id original, el nuevo tipo
    // y el mismo status que tenía antes de la actualización.
    @Test
    void execute_conPlazaExistente_deberiaActualizarTipoManteniendoIdYStatus() {
        UUID id = UUID.randomUUID();
        Spot existing = Spot.reconstruct(id, VehicleType.CAR, SpotStatus.OCCUPIED);
        SpotCreateDTO updateDTO = new SpotCreateDTO(VehicleType.CAR);

        when(spotPersistence.findById(id)).thenReturn(Optional.of(existing));
        when(spotPersistence.save(any(Spot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SpotDTO result = useCase.execute(id, updateDTO);

        assertEquals(id, result.getId());
        assertEquals(VehicleType.CAR, result.getType());
        assertEquals(SpotStatus.OCCUPIED, result.getStatus());
    }

    // Test: execute(id, updateDTO) debe guardar exactamente la plaza reconstruida
    // (mismo id, nuevo tipo, mismo status) mediante spotPersistence.save().
    // Resultado esperado: el Spot capturado en save() tiene el id, tipo y status
    // esperados.
    @Test
    void execute_deberiaGuardarLaPlazaReconstruidaConLosDatosCorrectos() {
        UUID id = UUID.randomUUID();
        Spot existing = Spot.reconstruct(id, VehicleType.MOTORBIKE, SpotStatus.AVAILABLE);
        SpotCreateDTO updateDTO = new SpotCreateDTO(VehicleType.CAR);
        ArgumentCaptor<Spot> spotCaptor = ArgumentCaptor.forClass(Spot.class);

        when(spotPersistence.findById(id)).thenReturn(Optional.of(existing));
        when(spotPersistence.save(any(Spot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(id, updateDTO);

        verify(spotPersistence).save(spotCaptor.capture());
        Spot captured = spotCaptor.getValue();
        assertEquals(id, captured.getId());
        assertEquals(VehicleType.CAR, captured.getType());
        assertEquals(SpotStatus.AVAILABLE, captured.getStatus());
    }

    // Test: execute(id, updateDTO) cuando no existe ninguna plaza con ese id
    // debe lanzar SpotNotFoundException y no debe intentar guardar nada.
    // Resultado esperado: se lanza SpotNotFoundException y save() nunca se invoca.
    @Test
    void execute_conPlazaInexistente_deberiaLanzarSpotNotFoundExceptionYNoGuardar() {
        UUID missingId = UUID.randomUUID();
        SpotCreateDTO updateDTO = new SpotCreateDTO(VehicleType.CAR);

        when(spotPersistence.findById(missingId)).thenReturn(Optional.empty());

        assertThrows(SpotNotFoundException.class, () -> useCase.execute(missingId, updateDTO));
        verify(spotPersistence, never()).save(any(Spot.class));
    }

    // Test: execute(id, updateDTO) no debe modificar el estado de la plaza,
    // incluso si esta se encuentra UNAVAILABLE; solo debe cambiar el tipo.
    // Resultado esperado: el status devuelto sigue siendo UNAVAILABLE tras la actualización.
    @Test
    void execute_conPlazaUnavailable_deberiaConservarEseStatusTrasActualizar() {
        UUID id = UUID.randomUUID();
        Spot existing = Spot.reconstruct(id, VehicleType.CAR, SpotStatus.UNAVAILABLE);
        SpotCreateDTO updateDTO = new SpotCreateDTO(VehicleType.CAR);

        when(spotPersistence.findById(id)).thenReturn(Optional.of(existing));
        when(spotPersistence.save(any(Spot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SpotDTO result = useCase.execute(id, updateDTO);

        assertEquals(SpotStatus.UNAVAILABLE, result.getStatus());
    }

    // Test: execute(id, updateDTO) con DTO con tipo nulo debe propagar InvalidSpotException
    // Resultado esperado: se lanza InvalidSpotException y no se invoca save().
    @Test
    void execute_conTipoNulo_deberiaLanzarExcepcion() {
        UUID id = UUID.randomUUID();
        Spot existing = Spot.reconstruct(id, VehicleType.CAR, SpotStatus.AVAILABLE);
        SpotCreateDTO updateDTO = new SpotCreateDTO(null);

        when(spotPersistence.findById(id)).thenReturn(Optional.of(existing));

        org.junit.jupiter.api.Assertions.assertThrows(
            com.tartis_recon_ai_parking.domain.spot.exception.InvalidSpotException.class, 
            () -> useCase.execute(id, updateDTO)
        );

        verify(spotPersistence, never()).save(any(Spot.class));
    }
}
