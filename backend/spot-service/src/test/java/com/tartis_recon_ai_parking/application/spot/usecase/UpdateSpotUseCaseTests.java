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

    @Test
    void execute_conPlazaExistente_deberiaActualizarTipoManteniendoIdYStatus() {
        // QUE HACE:
        // - Crea una plaza existente.
        // - Prepara un DTO con el nuevo tipo.
        // - Configura el mock para retornar la plaza y guardar los cambios.
        // - Ejecuta el método execute.
        UUID id = UUID.randomUUID();
        Spot existing = Spot.reconstruct(id, VehicleType.CAR, SpotStatus.OCCUPIED);
        SpotCreateDTO updateDTO = new SpotCreateDTO(VehicleType.CAR);

        when(spotPersistence.findById(id)).thenReturn(Optional.of(existing));
        when(spotPersistence.save(any(Spot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SpotDTO result = useCase.execute(id, updateDTO);

        // QUE DEBERIA HACER:
        // Debe retornar un DTO con el ID original, el estado que ya tenía, y el nuevo tipo de vehículo.
        assertEquals(id, result.getId());
        assertEquals(VehicleType.CAR, result.getType());
        assertEquals(SpotStatus.OCCUPIED, result.getStatus());
    }

    @Test
    void execute_deberiaGuardarLaPlazaReconstruidaConLosDatosCorrectos() {
        // QUE HACE:
        // - Configura un ArgumentCaptor para verificar qué objeto se guarda.
        // - Simula la plaza existente y la actualización con el DTO.
        // - Ejecuta la actualización.
        UUID id = UUID.randomUUID();
        Spot existing = Spot.reconstruct(id, VehicleType.MOTORBIKE, SpotStatus.AVAILABLE);
        SpotCreateDTO updateDTO = new SpotCreateDTO(VehicleType.CAR);
        ArgumentCaptor<Spot> spotCaptor = ArgumentCaptor.forClass(Spot.class);

        when(spotPersistence.findById(id)).thenReturn(Optional.of(existing));
        when(spotPersistence.save(any(Spot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(id, updateDTO);

        // QUE DEBERIA HACER:
        // Debe llamar a save() capturando la entidad.
        // La entidad capturada debe tener los datos correctamente reconstruidos (ID y estado anteriores, nuevo tipo).
        verify(spotPersistence).save(spotCaptor.capture());
        Spot captured = spotCaptor.getValue();
        assertEquals(id, captured.getId());
        assertEquals(VehicleType.CAR, captured.getType());
        assertEquals(SpotStatus.AVAILABLE, captured.getStatus());
    }

    @Test
    void execute_conPlazaInexistente_deberiaLanzarSpotNotFoundExceptionYNoGuardar() {
        // QUE HACE:
        // - Utiliza un ID que no existe en el sistema.
        // - Configura la persistencia para retornar vacío.
        // - Ejecuta execute.
        UUID missingId = UUID.randomUUID();
        SpotCreateDTO updateDTO = new SpotCreateDTO(VehicleType.CAR);

        when(spotPersistence.findById(missingId)).thenReturn(Optional.empty());

        assertThrows(SpotNotFoundException.class, () -> useCase.execute(missingId, updateDTO));
        
        // QUE DEBERIA HACER:
        // Debe lanzar SpotNotFoundException y nunca invocar al guardado.
        verify(spotPersistence, never()).save(any(Spot.class));
    }

    @Test
    void execute_conPlazaUnavailable_deberiaConservarEseStatusTrasActualizar() {
        // QUE HACE:
        // - Simula la actualización de una plaza con estado UNAVAILABLE.
        // - Ejecuta el método execute con el DTO correspondiente.
        UUID id = UUID.randomUUID();
        Spot existing = Spot.reconstruct(id, VehicleType.CAR, SpotStatus.UNAVAILABLE);
        SpotCreateDTO updateDTO = new SpotCreateDTO(VehicleType.CAR);

        when(spotPersistence.findById(id)).thenReturn(Optional.of(existing));
        when(spotPersistence.save(any(Spot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SpotDTO result = useCase.execute(id, updateDTO);

        // QUE DEBERIA HACER:
        // Debe mantener el estado UNAVAILABLE intacto.
        assertEquals(SpotStatus.UNAVAILABLE, result.getStatus());
    }

    @Test
    void execute_conTipoNulo_deberiaLanzarExcepcion() {
        // QUE HACE:
        // - Se prepara un DTO con tipo nulo.
        // - Se ejecuta la función de actualizar sobre una plaza válida.
        UUID id = UUID.randomUUID();
        Spot existing = Spot.reconstruct(id, VehicleType.CAR, SpotStatus.AVAILABLE);
        SpotCreateDTO updateDTO = new SpotCreateDTO(null);

        when(spotPersistence.findById(id)).thenReturn(Optional.of(existing));

        org.junit.jupiter.api.Assertions.assertThrows(
            com.tartis_recon_ai_parking.domain.spot.exception.InvalidSpotException.class, 
            () -> useCase.execute(id, updateDTO)
        );

        // QUE DEBERIA HACER:
        // Debe lanzar InvalidSpotException y asegurar que la plaza no se intentó persistir.
        verify(spotPersistence, never()).save(any(Spot.class));
    }
}
