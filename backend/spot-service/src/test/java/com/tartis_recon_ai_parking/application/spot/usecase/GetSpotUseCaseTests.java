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

    @Test
    void getById_conPlazaExistente_deberiaDevolverDtoCorrespondiente() {
        // QUE HACE:
        // - Crea una plaza de dominio de prueba.
        // - Configura el mock de la persistencia para devolverla al buscar por ID.
        // - Ejecuta el método getById del caso de uso.
        Spot spot = Spot.create(VehicleType.CAR);

        when(spotPersistence.findById(spot.getId())).thenReturn(Optional.of(spot));

        SpotDTO result = useCase.getById(spot.getId());

        // QUE DEBERIA HACER:
        // Debe devolver un SpotDTO que contenga exactamente los mismos datos de la plaza devuelta por la persistencia.
        assertEquals(spot.getId(), result.getId());
        assertEquals(spot.getType(), result.getType());
        assertEquals(spot.getStatus(), result.getStatus());
    }

    @Test
    void getById_conPlazaInexistente_deberiaLanzarSpotNotFoundException() {
        // QUE HACE:
        // - Genera un ID aleatorio.
        // - Configura el mock de la persistencia para devolver Optional vacío.
        // - Intenta obtener la plaza mediante getById.
        UUID missingId = UUID.randomUUID();

        when(spotPersistence.findById(missingId)).thenReturn(Optional.empty());

        // QUE DEBERIA HACER:
        // Debe lanzar una excepcion SpotNotFoundException indicando que la plaza no existe.
        assertThrows(SpotNotFoundException.class, () -> useCase.getById(missingId));
    }

    @Test
    void getById_deberiaConsultarPersistenciaConElIdExacto() {
        // QUE HACE:
        // - Genera un ID aleatorio y crea una plaza con él.
        // - Configura la persistencia para que devuelva la plaza al buscar con ese ID exacto.
        // - Llama al método getById.
        UUID id = UUID.randomUUID();
        Spot spot = Spot.reconstruct(id, VehicleType.CAR, SpotStatus.AVAILABLE);

        when(spotPersistence.findById(id)).thenReturn(Optional.of(spot));

        useCase.getById(id);

        // QUE DEBERIA HACER:
        // Debe consultar el repositorio de persistencia exactamente una vez utilizando el mismo ID recibido por parametro.
        verify(spotPersistence, times(1)).findById(id);
    }

    // =============  getAll  ============= //

    @Test
    void getAll_conVariasPlazas_deberiaDevolverListaDeDtosCorrespondiente() {
        // QUE HACE:
        // - Crea varias plazas de dominio y las agrupa en una lista.
        // - Configura la persistencia para devolver dicha lista al llamar a findAll.
        // - Llama al método getAll del caso de uso.
        Spot spot1 = Spot.create(VehicleType.CAR);
        Spot spot2 = Spot.create(VehicleType.MOTORBIKE);

        when(spotPersistence.findAll()).thenReturn(List.of(spot1, spot2));

        List<SpotDTO> result = useCase.getAll();

        // QUE DEBERIA HACER:
        // Debe retornar una lista de DTOs mapeada a partir de las entidades, respetando el tamaño y orden original.
        assertEquals(2, result.size());
        assertEquals(spot1.getId(), result.get(0).getId());
        assertEquals(spot2.getId(), result.get(1).getId());
    }

    @Test
    void getAll_sinPlazas_deberiaDevolverListaVacia() {
        // QUE HACE:
        // - Configura la persistencia para devolver una lista vacía.
        // - Invoca getAll del caso de uso.
        when(spotPersistence.findAll()).thenReturn(List.of());

        List<SpotDTO> result = useCase.getAll();

        // QUE DEBERIA HACER:
        // Debe retornar una lista vacía sin producir errores.
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
