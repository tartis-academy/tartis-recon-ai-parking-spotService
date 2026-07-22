package com.tartis_recon_ai_parking.application.spot.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.tartis_recon_ai_parking.application.spot.dto.SpotCreateDTO;
import com.tartis_recon_ai_parking.application.spot.dto.SpotDTO;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;

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

    // Test: execute() debe crear una nueva plaza AVAILABLE del tipo indicado
    // en el DTO, guardarla mediante SpotPersistence y devolver un SpotDTO
    // con los mismos datos que la plaza guardada.
    // Resultado esperado: se invoca save() una vez y el DTO devuelto refleja
    // el tipo y el estado AVAILABLE de la plaza creada.
    @Test
    void execute_deberiaCrearYGuardarPlazaDisponible() {
        SpotCreateDTO createDTO = new SpotCreateDTO(VehicleType.CAR);

        when(spotPersistence.save(any(Spot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SpotDTO result = useCase.execute(createDTO);

        verify(spotPersistence, times(1)).save(any(Spot.class));
        assertNotNull(result.getId());
        assertEquals(VehicleType.CAR, result.getType());
        assertEquals(SpotStatus.AVAILABLE, result.getStatus());
    }

    // Test: execute() debe pasar a spotPersistence.save() una instancia de Spot
    // construida con Spot.create(), es decir, con estado AVAILABLE y un id
    // generado automáticamente, y no una plaza con otro estado.
    // Resultado esperado: el Spot capturado en save() tiene status AVAILABLE
    // y un id no nulo.
    @Test
    void execute_deberiaConstruirSpotConEstadoAvailableAntesDeGuardar() {
        SpotCreateDTO createDTO = new SpotCreateDTO(VehicleType.MOTORBIKE);
        ArgumentCaptor<Spot> spotCaptor = ArgumentCaptor.forClass(Spot.class);

        when(spotPersistence.save(any(Spot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(createDTO);

        verify(spotPersistence).save(spotCaptor.capture());
        Spot captured = spotCaptor.getValue();
        assertNotNull(captured.getId());
        assertEquals(VehicleType.MOTORBIKE, captured.getType());
        assertEquals(SpotStatus.AVAILABLE, captured.getStatus());
    }

    // Test: el resultado de execute() debe basarse en la plaza devuelta por
    // spotPersistence.save() (por ejemplo, si la persistencia devolviera una
    // plaza con datos distintos a la enviada, el DTO reflejaría esos datos).
    // Resultado esperado: el SpotDTO devuelto coincide con la plaza "saved",
    // no con la que se pasó a save().
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

    // Test: execute() con DTO con tipo nulo debe propagar InvalidSpotException
    // Resultado esperado: se lanza InvalidSpotException y no se interactúa con la persistencia.
    @Test
    void execute_deberiaLanzarExcepcionConTipoNulo() {
        SpotCreateDTO createDTO = new SpotCreateDTO(null);
        
        org.junit.jupiter.api.Assertions.assertThrows(
            com.tartis_recon_ai_parking.domain.spot.exception.InvalidSpotException.class, 
            () -> useCase.execute(createDTO)
        );

        verify(spotPersistence, never()).save(any(Spot.class));
    }
}
