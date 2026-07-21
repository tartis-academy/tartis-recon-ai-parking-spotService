package com.tartis_recon_ai_parking.domain.spot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tartis_recon_ai_parking.domain.spot.exception.InvalidSpotException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la clase Spot.
 *
 * Requisitos en el pom.xml (JUnit 5):
 *
 * <dependency>
 *     <groupId>org.junit.jupiter</groupId>
 *     <artifactId>junit-jupiter</artifactId>
 *     <version>5.10.2</version>
 *     <scope>test</scope>
 * </dependency>
 *
 * Y el plugin de Surefire (normalmente ya viene por defecto en Maven,
 * pero conviene fijar una versión reciente, p.ej. 3.2.5).
 */
public class SpotTest {

    private UUID sampleId;
    private VehicleType sampleType;

    @BeforeEach
    void setUp() {
        sampleId = UUID.randomUUID();
        sampleType = VehicleType.CAR;
    }

    // =============  CREACIÓN  ============= //

    // Test: Spot.create(type) debe generar una plaza nueva con estado AVAILABLE
    // y un id no nulo asignado automáticamente.
    // Resultado esperado: getStatus() == AVAILABLE, getId() != null, getType() == type indicado.
    @Test
    void create_deberiaCrearPlazaDisponibleConIdGenerado() {
        Spot spot = Spot.create(sampleType);

        assertNotNull(spot.getId());
        assertEquals(sampleType, spot.getType());
        assertEquals(SpotStatus.AVAILABLE, spot.getStatus());
    }

    // Test: Spot.reconstruct(id, type, status) debe crear una plaza reutilizando
    // datos externos (p.ej. venidos de base de datos) sin modificarlos.
    // Resultado esperado: los getters devuelven exactamente los valores pasados.
    @Test
    void reconstruct_deberiaCrearPlazaConDatosExternos() {
        Spot spot = Spot.reconstruct(sampleId, sampleType, SpotStatus.OCCUPIED);

        assertEquals(sampleId, spot.getId());
        assertEquals(sampleType, spot.getType());
        assertEquals(SpotStatus.OCCUPIED, spot.getStatus());
    }

    // Test: reconstruct con id nulo debe lanzar InvalidSpotException,
    // ya que el constructor privado valida que el id no sea null.
    // Resultado esperado: se lanza InvalidSpotException.
    @Test
    void reconstruct_conIdNulo_deberiaLanzarExcepcion() {
        assertThrows(InvalidSpotException.class,
                () -> Spot.reconstruct(null, sampleType, SpotStatus.AVAILABLE));
    }

    // Test: reconstruct con type nulo debe lanzar InvalidSpotException.
    // Resultado esperado: se lanza InvalidSpotException.
    @Test
    void reconstruct_conTypeNulo_deberiaLanzarExcepcion() {
        assertThrows(InvalidSpotException.class,
                () -> Spot.reconstruct(sampleId, null, SpotStatus.AVAILABLE));
    }

    // Test: reconstruct con status nulo debe lanzar InvalidSpotException.
    // Resultado esperado: se lanza InvalidSpotException.
    @Test
    void reconstruct_conStatusNulo_deberiaLanzarExcepcion() {
        assertThrows(InvalidSpotException.class,
                () -> Spot.reconstruct(sampleId, sampleType, null));
    }


    // =============  OCCUPY()  ============= //

    // Test: occupy() sobre una plaza AVAILABLE debe cambiar su estado a OCCUPIED.
    // Resultado esperado: getStatus() == OCCUPIED tras la llamada.
    @Test
    void occupy_desdeAvailable_deberiaCambiarAOccupied() {
        Spot spot = Spot.create(sampleType);

        spot.occupy();

        assertEquals(SpotStatus.OCCUPIED, spot.getStatus());
    }

    // Test: occupy() sobre una plaza ya OCCUPIED debe lanzar InvalidSpotException,
    // ya que solo se puede ocupar una plaza DISPONIBLE.
    // Resultado esperado: se lanza InvalidSpotException y el estado no cambia.
    @Test
    void occupy_desdeOccupied_deberiaLanzarExcepcion() {
        Spot spot = Spot.reconstruct(sampleId, sampleType, SpotStatus.OCCUPIED);

        assertThrows(InvalidSpotException.class, spot::occupy);
        assertEquals(SpotStatus.OCCUPIED, spot.getStatus());
    }

    // Test: occupy() sobre una plaza UNAVAILABLE debe lanzar InvalidSpotException.
    // Resultado esperado: se lanza InvalidSpotException y el estado permanece UNAVAILABLE.
    @Test
    void occupy_desdeUnavailable_deberiaLanzarExcepcion() {
        Spot spot = Spot.reconstruct(sampleId, sampleType, SpotStatus.UNAVAILABLE);

        assertThrows(InvalidSpotException.class, spot::occupy);
        assertEquals(SpotStatus.UNAVAILABLE, spot.getStatus());
    }


    // =============  RELEASE()  ============= //

    // Test: release() sobre una plaza OCCUPIED debe cambiar su estado a AVAILABLE.
    // Resultado esperado: getStatus() == AVAILABLE tras la llamada.
    @Test
    void release_desdeOccupied_deberiaCambiarAAvailable() {
        Spot spot = Spot.reconstruct(sampleId, sampleType, SpotStatus.OCCUPIED);

        spot.release();

        assertEquals(SpotStatus.AVAILABLE, spot.getStatus());
    }

    // Test: release() sobre una plaza AVAILABLE debe lanzar InvalidSpotException,
    // ya que solo se puede liberar una plaza que esté OCUPADA.
    // Resultado esperado: se lanza InvalidSpotException y el estado permanece AVAILABLE.
    @Test
    void release_desdeAvailable_deberiaLanzarExcepcion() {
        Spot spot = Spot.create(sampleType);

        assertThrows(InvalidSpotException.class, spot::release);
        assertEquals(SpotStatus.AVAILABLE, spot.getStatus());
    }

    // Test: release() sobre una plaza UNAVAILABLE debe lanzar InvalidSpotException.
    // Resultado esperado: se lanza InvalidSpotException y el estado permanece UNAVAILABLE.
    @Test
    void release_desdeUnavailable_deberiaLanzarExcepcion() {
        Spot spot = Spot.reconstruct(sampleId, sampleType, SpotStatus.UNAVAILABLE);

        assertThrows(InvalidSpotException.class, spot::release);
        assertEquals(SpotStatus.UNAVAILABLE, spot.getStatus());
    }


    // =============  BLOCKFORMAINTENANCE()  ============= //

    // Test: blockForMaintenance() sobre una plaza AVAILABLE debe cambiar su estado a UNAVAILABLE.
    // Resultado esperado: getStatus() == UNAVAILABLE tras la llamada.
    @Test
    void blockForMaintenance_desdeAvailable_deberiaCambiarAUnavailable() {
        Spot spot = Spot.create(sampleType);

        spot.blockForMaintenance();

        assertEquals(SpotStatus.UNAVAILABLE, spot.getStatus());
    }

    // Test: blockForMaintenance() sobre una plaza OCCUPIED debe lanzar InvalidSpotException,
    // ya que solo se puede bloquear una plaza que esté DISPONIBLE.
    // Resultado esperado: se lanza InvalidSpotException y el estado permanece OCCUPIED.
    @Test
    void blockForMaintenance_desdeOccupied_deberiaLanzarExcepcion() {
        Spot spot = Spot.reconstruct(sampleId, sampleType, SpotStatus.OCCUPIED);

        assertThrows(InvalidSpotException.class, spot::blockForMaintenance);
        assertEquals(SpotStatus.OCCUPIED, spot.getStatus());
    }

    // Test: blockForMaintenance() sobre una plaza ya UNAVAILABLE debe lanzar InvalidSpotException.
    // Resultado esperado: se lanza InvalidSpotException y el estado permanece UNAVAILABLE.
    @Test
    void blockForMaintenance_desdeUnavailable_deberiaLanzarExcepcion() {
        Spot spot = Spot.reconstruct(sampleId, sampleType, SpotStatus.UNAVAILABLE);

        assertThrows(InvalidSpotException.class, spot::blockForMaintenance);
        assertEquals(SpotStatus.UNAVAILABLE, spot.getStatus());
    }


    // =============  UNBLOCK()  ============= //

    // Test: unblock() sobre una plaza UNAVAILABLE debe cambiar su estado a AVAILABLE.
    // Resultado esperado: getStatus() == AVAILABLE tras la llamada.
    @Test
    void unblock_desdeUnavailable_deberiaCambiarAAvailable() {
        Spot spot = Spot.reconstruct(sampleId, sampleType, SpotStatus.UNAVAILABLE);

        spot.unblock();

        assertEquals(SpotStatus.AVAILABLE, spot.getStatus());
    }

    // Test: unblock() sobre una plaza AVAILABLE debe lanzar InvalidSpotException,
    // ya que solo se puede desbloquear una plaza que esté NO DISPONIBLE.
    // Resultado esperado: se lanza InvalidSpotException y el estado permanece AVAILABLE.
    @Test
    void unblock_desdeAvailable_deberiaLanzarExcepcion() {
        Spot spot = Spot.create(sampleType);

        assertThrows(InvalidSpotException.class, spot::unblock);
        assertEquals(SpotStatus.AVAILABLE, spot.getStatus());
    }

    // Test: unblock() sobre una plaza OCCUPIED debe lanzar InvalidSpotException.
    // Resultado esperado: se lanza InvalidSpotException y el estado permanece OCCUPIED.
    @Test
    void unblock_desdeOccupied_deberiaLanzarExcepcion() {
        Spot spot = Spot.reconstruct(sampleId, sampleType, SpotStatus.OCCUPIED);

        assertThrows(InvalidSpotException.class, spot::unblock);
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