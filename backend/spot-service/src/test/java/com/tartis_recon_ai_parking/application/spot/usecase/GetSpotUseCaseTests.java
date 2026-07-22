package com.tartis_recon_ai_parking.application.spot.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tartis_recon_ai_parking.application.spot.dto.SpotDTO;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * Tests unitarios para GetSpotUseCase.
 * Requiere las mismas dependencias de Mockito indicadas en CreateSpotUseCaseTests.
 */
@ExtendWith(MockitoExtension.class)
public class GetSpotUseCaseTests {

    @Mock
    private SpotPersistence spotPersistence;

    private GetSpotUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetSpotUseCase(spotPersistence);
    }

    // =============  getById  ============= //

    // Test: getById(id) cuando la plaza existe debe devolver un SpotDTO
    // con los mismos datos (id, tipo y estado) que la plaza encontrada.
    // Resultado esperado: el DTO devuelto refleja exactamente los datos de la plaza.
    @Test
    void getById_conPlazaExistente_deberiaDevolverDtoCorrespondiente() {
        Spot spot = Spot.create(VehicleType.CAR);

        when(spotPersistence.findById(spot.getId())).thenReturn(Optional.of(spot));

        SpotDTO result = useCase.getById(spot.getId());

        assertEquals(spot.getId(), result.getId());
        assertEquals(spot.getType(), result.getType());
        assertEquals(spot.getStatus(), result.getStatus());
    }

    // Test: getById(id) cuando no existe ninguna plaza con ese id debe lanzar
    // SpotNotFoundException en lugar de devolver null o un DTO vacío.
    // Resultado esperado: se lanza SpotNotFoundException.
    @Test
    void getById_conPlazaInexistente_deberiaLanzarSpotNotFoundException() {
        UUID missingId = UUID.randomUUID();

        when(spotPersistence.findById(missingId)).thenReturn(Optional.empty());

        assertThrows(SpotNotFoundException.class, () -> useCase.getById(missingId));
    }

    // Test: getById(id) debe consultar spotPersistence.findById() exactamente
    // con el id recibido, sin transformarlo.
    // Resultado esperado: findById() se invoca una única vez con el id exacto.
    @Test
    void getById_deberiaConsultarPersistenciaConElIdExacto() {
        UUID id = UUID.randomUUID();
        Spot spot = Spot.reconstruct(id, VehicleType.CAR, SpotStatus.AVAILABLE);

        when(spotPersistence.findById(id)).thenReturn(Optional.of(spot));

        useCase.getById(id);

        verify(spotPersistence, times(1)).findById(id);
    }

    // =============  getAll  ============= //

    // Test: getAll() debe devolver una lista de SpotDTO con el mismo tamaño
    // y datos que las plazas devueltas por spotPersistence.findAll().
    // Resultado esperado: la lista devuelta tiene el mismo tamaño que la fuente
    // y cada DTO corresponde a la plaza en la misma posición.
    @Test
    void getAll_conVariasPlazas_deberiaDevolverListaDeDtosCorrespondiente() {
        Spot spot1 = Spot.create(VehicleType.CAR);
        Spot spot2 = Spot.create(VehicleType.MOTORBIKE);

        when(spotPersistence.findAll()).thenReturn(List.of(spot1, spot2));

        List<SpotDTO> result = useCase.getAll();

        assertEquals(2, result.size());
        assertEquals(spot1.getId(), result.get(0).getId());
        assertEquals(spot2.getId(), result.get(1).getId());
    }

    // Test: getAll() cuando no hay plazas registradas debe devolver una lista
    // vacía, sin lanzar excepciones.
    // Resultado esperado: la lista devuelta está vacía (size == 0).
    @Test
    void getAll_sinPlazas_deberiaDevolverListaVacia() {
        when(spotPersistence.findAll()).thenReturn(List.of());

        List<SpotDTO> result = useCase.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
