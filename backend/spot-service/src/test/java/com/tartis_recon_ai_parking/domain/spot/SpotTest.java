package com.tartis_recon_ai_parking.domain.spot;

import com.tartis_recon_ai_parking.domain.spot.exception.InvalidSpotException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SpotTest {

    private static final VehicleType VALID_TYPE = VehicleType.CAR;
    private static final Integer VALID_NUM_SPOT = 12;

    @Nested
    @DisplayName("Constructor de alta de plaza nueva")
    class CreationConstructor {

        @Test
        @DisplayName("Crea una plaza válida en estado AVAILABLE por defecto")
        void createsSpotWithAvailableStatus() {
            UUID id = UUID.randomUUID();

            Spot spot = new Spot(id, VALID_TYPE, VALID_NUM_SPOT);

            assertEquals(id, spot.getId());
            assertEquals(VALID_TYPE, spot.getType());
            assertEquals(VALID_NUM_SPOT, spot.getNumSpot());
            assertEquals(SpotStatus.AVAILABLE, spot.getStatus());
        }

        @Test
        @DisplayName("Lanza excepción si el id es nulo")
        void throwsWhenIdIsNull() {
            assertThrows(InvalidSpotException.class,
                    () -> new Spot(null, VALID_TYPE, VALID_NUM_SPOT));
        }

        @Test
        @DisplayName("Lanza excepción si el type es nulo")
        void throwsWhenTypeIsNull() {
            assertThrows(InvalidSpotException.class,
                    () -> new Spot(UUID.randomUUID(), null, VALID_NUM_SPOT));
        }

        @Test
        @DisplayName("Lanza excepción si numSpot es nulo")
        void throwsWhenNumSpotIsNull() {
            assertThrows(InvalidSpotException.class,
                    () -> new Spot(UUID.randomUUID(), VALID_TYPE, null));
        }

        @Test
        @DisplayName("Lanza excepción si numSpot es cero")
        void throwsWhenNumSpotIsZero() {
            assertThrows(InvalidSpotException.class,
                    () -> new Spot(UUID.randomUUID(), VALID_TYPE, 0));
        }

        @Test
        @DisplayName("Lanza excepción si numSpot es negativo")
        void throwsWhenNumSpotIsNegative() {
            assertThrows(InvalidSpotException.class,
                    () -> new Spot(UUID.randomUUID(), VALID_TYPE, -5));
        }
    }

    @Nested
    @DisplayName("Constructor de reconstrucción (con status explícito)")
    class RehydrationConstructor {

        @Test
        @DisplayName("Reconstruye una plaza respetando el status recibido")
        void rehydratesSpotWithGivenStatus() {
            Spot spot = new Spot(UUID.randomUUID(), VALID_TYPE, VALID_NUM_SPOT, SpotStatus.OCCUPIED);

            assertEquals(SpotStatus.OCCUPIED, spot.getStatus());
        }

        @Test
        @DisplayName("Lanza excepción si el status es nulo")
        void throwsWhenStatusIsNull() {
            assertThrows(InvalidSpotException.class,
                    () -> new Spot(UUID.randomUUID(), VALID_TYPE, VALID_NUM_SPOT, null));
        }

        @Test
        @DisplayName("Sigue validando id, type y numSpot igual que el constructor de alta")
        void stillValidatesBasicFields() {
            assertThrows(InvalidSpotException.class,
                    () -> new Spot(null, VALID_TYPE, VALID_NUM_SPOT, SpotStatus.AVAILABLE));
            assertThrows(InvalidSpotException.class,
                    () -> new Spot(UUID.randomUUID(), null, VALID_NUM_SPOT, SpotStatus.AVAILABLE));
            assertThrows(InvalidSpotException.class,
                    () -> new Spot(UUID.randomUUID(), VALID_TYPE, 0, SpotStatus.AVAILABLE));
        }
    }

    @Nested
    @DisplayName("occupy()")
    class Occupy {

        @Test
        @DisplayName("Ocupa una plaza que está AVAILABLE")
        void occupiesAvailableSpot() {
            Spot spot = new Spot(UUID.randomUUID(), VALID_TYPE, VALID_NUM_SPOT);

            spot.occupy();

            assertEquals(SpotStatus.OCCUPIED, spot.getStatus());
        }

        @Test
        @DisplayName("RN-05: no permite ocupar una plaza que ya está OCCUPIED")
        void rejectsOccupyingAlreadyOccupiedSpot() {
            Spot spot = new Spot(UUID.randomUUID(), VALID_TYPE, VALID_NUM_SPOT, SpotStatus.OCCUPIED);

            assertThrows(InvalidSpotException.class, spot::occupy);
        }

        @Test
        @DisplayName("No permite ocupar una plaza UNAVAILABLE")
        void rejectsOccupyingUnavailableSpot() {
            Spot spot = new Spot(UUID.randomUUID(), VALID_TYPE, VALID_NUM_SPOT, SpotStatus.UNAVAILABLE);

            assertThrows(InvalidSpotException.class, spot::occupy);
        }
    }

    @Nested
    @DisplayName("release()")
    class Release {

        @Test
        @DisplayName("Libera una plaza que está OCCUPIED")
        void releasesOccupiedSpot() {
            Spot spot = new Spot(UUID.randomUUID(), VALID_TYPE, VALID_NUM_SPOT, SpotStatus.OCCUPIED);

            spot.release();

            assertEquals(SpotStatus.AVAILABLE, spot.getStatus());
        }

        @Test
        @DisplayName("No permite liberar una plaza que ya está AVAILABLE")
        void rejectsReleasingAvailableSpot() {
            Spot spot = new Spot(UUID.randomUUID(), VALID_TYPE, VALID_NUM_SPOT);

            assertThrows(InvalidSpotException.class, spot::release);
        }

        @Test
        @DisplayName("No permite liberar una plaza UNAVAILABLE")
        void rejectsReleasingUnavailableSpot() {
            Spot spot = new Spot(UUID.randomUUID(), VALID_TYPE, VALID_NUM_SPOT, SpotStatus.UNAVAILABLE);

            assertThrows(InvalidSpotException.class, spot::release);
        }
    }

    @Nested
    @DisplayName("blockForMaintenance()")
    class BlockForMaintenance {

        @Test
        @DisplayName("RN-10: bloquea una plaza que está AVAILABLE")
        void blocksAvailableSpot() {
            Spot spot = new Spot(UUID.randomUUID(), VALID_TYPE, VALID_NUM_SPOT);

            spot.blockForMaintenance();

            assertEquals(SpotStatus.UNAVAILABLE, spot.getStatus());
        }

        @Test
        @DisplayName("No permite bloquear una plaza OCCUPIED")
        void rejectsBlockingOccupiedSpot() {
            Spot spot = new Spot(UUID.randomUUID(), VALID_TYPE, VALID_NUM_SPOT, SpotStatus.OCCUPIED);

            assertThrows(InvalidSpotException.class, spot::blockForMaintenance);
        }

        @Test
        @DisplayName("No permite bloquear una plaza que ya está UNAVAILABLE")
        void rejectsBlockingAlreadyUnavailableSpot() {
            Spot spot = new Spot(UUID.randomUUID(), VALID_TYPE, VALID_NUM_SPOT, SpotStatus.UNAVAILABLE);

            assertThrows(InvalidSpotException.class, spot::blockForMaintenance);
        }
    }

    @Nested
    @DisplayName("unblock()")
    class Unblock {

        @Test
        @DisplayName("Desbloquea una plaza que está UNAVAILABLE")
        void unblocksUnavailableSpot() {
            Spot spot = new Spot(UUID.randomUUID(), VALID_TYPE, VALID_NUM_SPOT, SpotStatus.UNAVAILABLE);

            spot.unblock();

            assertEquals(SpotStatus.AVAILABLE, spot.getStatus());
        }

        @Test
        @DisplayName("No permite desbloquear una plaza AVAILABLE")
        void rejectsUnblockingAvailableSpot() {
            Spot spot = new Spot(UUID.randomUUID(), VALID_TYPE, VALID_NUM_SPOT);

            assertThrows(InvalidSpotException.class, spot::unblock);
        }

        @Test
        @DisplayName("No permite desbloquear una plaza OCCUPIED")
        void rejectsUnblockingOccupiedSpot() {
            Spot spot = new Spot(UUID.randomUUID(), VALID_TYPE, VALID_NUM_SPOT, SpotStatus.OCCUPIED);

            assertThrows(InvalidSpotException.class, spot::unblock);
        }
    }

    @Test
    @DisplayName("IN-04: el ciclo completo de transiciones siempre deja un único estado válido")
    void fullLifecycleAlwaysHasExactlyOneStatus() {
        Spot spot = new Spot(UUID.randomUUID(), VALID_TYPE, VALID_NUM_SPOT);
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

    @Nested
    @DisplayName("equals() y hashCode()")
    class EqualityById {

        @Test
        @DisplayName("Dos Spot con el mismo id son iguales aunque cambie el status")
        void spotsWithSameIdAreEqualRegardlessOfStatus() {
            UUID sharedId = UUID.randomUUID();
            Spot available = new Spot(sharedId, VALID_TYPE, VALID_NUM_SPOT, SpotStatus.AVAILABLE);
            Spot occupied = new Spot(sharedId, VALID_TYPE, VALID_NUM_SPOT, SpotStatus.OCCUPIED);

            assertEquals(available, occupied);
            assertEquals(available.hashCode(), occupied.hashCode());
        }

        @Test
        @DisplayName("Dos Spot con id distinto no son iguales aunque el resto de datos coincida")
        void spotsWithDifferentIdAreNotEqual() {
            Spot spot1 = new Spot(UUID.randomUUID(), VALID_TYPE, VALID_NUM_SPOT);
            Spot spot2 = new Spot(UUID.randomUUID(), VALID_TYPE, VALID_NUM_SPOT);

            assertNotEquals(spot1, spot2);
        }
    }

    @Nested
    @DisplayName("isAvailableFor()")
    class IsAvailableFor {

        @Test
        @DisplayName("Devuelve true si está AVAILABLE y el tipo coincide (HU-03)")
        void trueWhenAvailableAndTypeMatches() {
            Spot spot = new Spot(UUID.randomUUID(), VehicleType.CAR, VALID_NUM_SPOT);

            assertTrue(spot.isAvailableFor(VehicleType.CAR));
        }

        @Test
        @DisplayName("Devuelve false si el tipo no coincide")
        void falseWhenTypeDoesNotMatch() {
            Spot spot = new Spot(UUID.randomUUID(), VehicleType.CAR, VALID_NUM_SPOT);

            assertFalse(spot.isAvailableFor(VehicleType.MOTORBIKE));
        }

        @Test
        @DisplayName("Devuelve false si la plaza no está AVAILABLE aunque el tipo coincida")
        void falseWhenNotAvailable() {
            Spot spot = new Spot(UUID.randomUUID(), VehicleType.CAR, VALID_NUM_SPOT, SpotStatus.OCCUPIED);

            assertFalse(spot.isAvailableFor(VehicleType.CAR));
        }
    }
}