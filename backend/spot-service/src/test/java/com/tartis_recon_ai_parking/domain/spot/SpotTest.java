package com.tartis_recon_ai_parking.domain.spot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tartis_recon_ai_parking.domain.spot.exception.SpotAlreadyOccupiedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotCannotBeBlockedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotBlockedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotOccupiedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotValidationException;

import java.util.UUID;

public class SpotTest {

    private UUID sampleId;
    private VehicleType sampleType;

    @BeforeEach
    void setUp() {
        sampleId = UUID.randomUUID();
        sampleType = VehicleType.CAR;
    }

    // =============  CREACIÓN  ============= //

    @Test
    void create_deberiaCrearPlazaDisponibleConIdGenerado() {
        Spot spot = Spot.create(sampleType);

        assertNotNull(spot.getId());
        assertEquals(sampleType, spot.getType());
        assertEquals(SpotStatus.AVAILABLE, spot.getStatus());
    }

    @Test
    void reconstruct_deberiaCrearPlazaConDatosExternos() {
        Spot spot = Spot.reconstruct(sampleId, sampleType, SpotStatus.OCCUPIED);

        assertEquals(sampleId, spot.getId());
        assertEquals(sampleType, spot.getType());
        assertEquals(SpotStatus.OCCUPIED, spot.getStatus());
    }

    @Test
    void reconstruct_conIdNulo_deberiaLanzarExcepcion() {
        assertThrows(SpotValidationException.class,
                () -> Spot.reconstruct(null, sampleType, SpotStatus.AVAILABLE));
    }

    @Test
    void reconstruct_conTypeNulo_deberiaLanzarExcepcion() {
        assertThrows(SpotValidationException.class,
                () -> Spot.reconstruct(sampleId, null, SpotStatus.AVAILABLE));
    }

    @Test
    void reconstruct_conStatusNulo_deberiaLanzarExcepcion() {
        assertThrows(SpotValidationException.class,
                () -> Spot.reconstruct(sampleId, sampleType, null));
    }


    // =============  OCCUPY()  ============= //

    @Test
    void occupy_desdeAvailable_deberiaCambiarAOccupied() {
        Spot spot = Spot.create(sampleType);

        spot.occupy();

        assertEquals(SpotStatus.OCCUPIED, spot.getStatus());
    }

    @Test
    void occupy_desdeOccupied_deberiaLanzarExcepcion() {
        Spot spot = Spot.reconstruct(sampleId, sampleType, SpotStatus.OCCUPIED);

        assertThrows(SpotAlreadyOccupiedException.class, spot::occupy);
        assertEquals(SpotStatus.OCCUPIED, spot.getStatus());
    }

    @Test
    void occupy_desdeUnavailable_deberiaLanzarExcepcion() {
        Spot spot = Spot.reconstruct(sampleId, sampleType, SpotStatus.UNAVAILABLE);

        assertThrows(SpotAlreadyOccupiedException.class, spot::occupy);
        assertEquals(SpotStatus.UNAVAILABLE, spot.getStatus());
    }


    // =============  RELEASE()  ============= //

    @Test
    void release_desdeOccupied_deberiaCambiarAAvailable() {
        Spot spot = Spot.reconstruct(sampleId, sampleType, SpotStatus.OCCUPIED);

        spot.release();

        assertEquals(SpotStatus.AVAILABLE, spot.getStatus());
    }

    @Test
    void release_desdeAvailable_deberiaLanzarExcepcion() {
        Spot spot = Spot.create(sampleType);

        assertThrows(SpotNotOccupiedException.class, spot::release);
        assertEquals(SpotStatus.AVAILABLE, spot.getStatus());
    }

    @Test
    void release_desdeUnavailable_deberiaLanzarExcepcion() {
        Spot spot = Spot.reconstruct(sampleId, sampleType, SpotStatus.UNAVAILABLE);

        assertThrows(SpotNotOccupiedException.class, spot::release);
        assertEquals(SpotStatus.UNAVAILABLE, spot.getStatus());
    }


    // =============  BLOCKFORMAINTENANCE()  ============= //

    @Test
    void blockForMaintenance_desdeAvailable_deberiaCambiarAUnavailable() {
        Spot spot = Spot.create(sampleType);

        spot.blockForMaintenance();

        assertEquals(SpotStatus.UNAVAILABLE, spot.getStatus());
    }

    @Test
    void blockForMaintenance_desdeOccupied_deberiaLanzarExcepcion() {
        Spot spot = Spot.reconstruct(sampleId, sampleType, SpotStatus.OCCUPIED);

        assertThrows(SpotCannotBeBlockedException.class, spot::blockForMaintenance);
        assertEquals(SpotStatus.OCCUPIED, spot.getStatus());
    }

    @Test
    void blockForMaintenance_desdeUnavailable_deberiaLanzarExcepcion() {
        Spot spot = Spot.reconstruct(sampleId, sampleType, SpotStatus.UNAVAILABLE);

        assertThrows(SpotCannotBeBlockedException.class, spot::blockForMaintenance);
        assertEquals(SpotStatus.UNAVAILABLE, spot.getStatus());
    }


    // =============  UNBLOCK()  ============= //

    @Test
    void unblock_desdeUnavailable_deberiaCambiarAAvailable() {
        Spot spot = Spot.reconstruct(sampleId, sampleType, SpotStatus.UNAVAILABLE);

        spot.unblock();

        assertEquals(SpotStatus.AVAILABLE, spot.getStatus());
    }

    @Test
    void unblock_desdeAvailable_deberiaLanzarExcepcion() {
        Spot spot = Spot.create(sampleType);

        assertThrows(SpotNotBlockedException.class, spot::unblock);
        assertEquals(SpotStatus.AVAILABLE, spot.getStatus());
    }

    @Test
    void unblock_desdeOccupied_deberiaLanzarExcepcion() {
        Spot spot = Spot.reconstruct(sampleId, sampleType, SpotStatus.OCCUPIED);

        assertThrows(SpotNotBlockedException.class, spot::unblock);
        assertEquals(SpotStatus.OCCUPIED, spot.getStatus());
    }


    // =============  CICLO COMPLETO  ============= //

    // Test: una plaza debe poder pasar por el ciclo completo
    // AVAILABLE -> OCCUPIED -> AVAILABLE -> UNAVAILABLE -> AVAILABLE sin errores.
    // Resultado esperado: en cada paso el estado final es el esperado.
    @Test
    void cicloCompletoDeEstados_deberiaFuncionarSinErrores() {
        Spot spot = Spot.create(sampleType);
        assertEquals(SpotStatus.AVAILABLE, spot.getStatus());

        spot.occupy();
        assertEquals(SpotStatus.OCCUPIED, spot.getStatus());

        spot.release();
        assertEquals(SpotStatus.AVAILABLE, spot.getStatus());

        spot.blockForMaintenance();
        assertEquals(SpotStatus.UNAVAILABLE, spot.getStatus());

        spot.unblock();
        assertEquals(SpotStatus.AVAILABLE, spot.getStatus());
    }
}