package com.tartis_recon_ai_parking.application.spot.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tartis_recon_ai_parking.application.spot.dto.SpotCreateDTO;
import com.tartis_recon_ai_parking.application.spot.dto.SpotDTO;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CreateSpotUseCase.
 *
 * Dependencias necesarias en el pom.xml (además de junit-jupiter):
 *
 * <dependency>
 *     <groupId>org.mockito</groupId>
 *     <artifactId>mockito-core</artifactId>
 *     <version>5.11.0</version>
 *     <scope>test</scope>
 * </dependency>
 * <dependency>
 *     <groupId>org.mockito</groupId>
 *     <artifactId>mockito-junit-jupiter</artifactId>
 *     <version>5.11.0</version>
 *     <scope>test</scope>
 * </dependency>
 */
@ExtendWith(MockitoExtension.class)
public class CreateSpotUseCaseTests {

    @Mock
    private SpotPersistence spotPersistence;

    private CreateSpotUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateSpotUseCase(spotPersistence);
    }

    @Test
    void execute_deberiaCrearYGuardarPlazaDisponible() {
        // QUE HACE:
        // - Crea un DTO con los datos para una nueva plaza.
        // - Configura el mock de la persistencia para simular el guardado.
        // - Ejecuta el método execute del caso de uso.
        SpotCreateDTO createDTO = new SpotCreateDTO(VehicleType.CAR);

        when(spotPersistence.save(any(Spot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SpotDTO result = useCase.execute(createDTO);

        // QUE DEBERIA HACER:
        // Debe verificar que se invocó save() en persistencia exactamente una vez.
        // El DTO resultante debe reflejar el tipo y el estado de la plaza recién creada (AVAILABLE).
        verify(spotPersistence, times(1)).save(any(Spot.class));
        assertNotNull(result.getId());
        assertEquals(VehicleType.CAR, result.getType());
        assertEquals(SpotStatus.AVAILABLE, result.getStatus());
    }

    @Test
    void execute_deberiaConstruirSpotConEstadoAvailableAntesDeGuardar() {
        // QUE HACE:
        // - Prepara un DTO y un ArgumentCaptor para interceptar el objeto guardado.
        // - Ejecuta la creación de la plaza.
        SpotCreateDTO createDTO = new SpotCreateDTO(VehicleType.MOTORBIKE);
        ArgumentCaptor<Spot> spotCaptor = ArgumentCaptor.forClass(Spot.class);

        when(spotPersistence.save(any(Spot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(createDTO);

        // QUE DEBERIA HACER:
        // Debe asegurar que la entidad que se pasa a guardar tenga estado AVAILABLE y un ID generado.
        verify(spotPersistence).save(spotCaptor.capture());
        Spot captured = spotCaptor.getValue();
        assertNotNull(captured.getId());
        assertEquals(VehicleType.MOTORBIKE, captured.getType());
        assertEquals(SpotStatus.AVAILABLE, captured.getStatus());
    }

    @Test
    void execute_deberiaDevolverDtoBasadoEnLaPlazaGuardadaDevueltaPorPersistencia() {
        SpotCreateDTO createDTO = new SpotCreateDTO(VehicleType.CAR);
        Spot persistedSpot = Spot.create(VehicleType.CAR);
        persistedSpot.occupy();

        when(spotPersistence.save(any(Spot.class))).thenReturn(persistedSpot);

        SpotDTO result = useCase.execute(createDTO);

        assertEquals(persistedSpot.getId(), result.getId());
        assertEquals(SpotStatus.OCCUPIED, result.getStatus());
    }

    @Test
    void execute_deberiaLanzarExcepcionConTipoNulo() {

        SpotCreateDTO createDTO = new SpotCreateDTO(null);
        
        assertThrows(
            SpotValidationException.class, 
            () -> useCase.execute(createDTO)
        );

    
        verify(spotPersistence, never()).save(any(Spot.class));
    }
}
