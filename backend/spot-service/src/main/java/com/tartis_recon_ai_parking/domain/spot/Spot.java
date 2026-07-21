package com.tartis_recon_ai_parking.domain.spot;

import com.tartis_recon_ai_parking.domain.spot.exception.InvalidSpotException;
import java.util.UUID;

public final class Spot {

    private final UUID id;
    private final VehicleType type;
    private SpotStatus status;

    //Creates a new spot with its own ID.
    public static Spot create(VehicleType type, SpotStatus status){
        return new Spot(UUID.randomUUID(), type, status);
    }

    //Create a new spot with external data.
    public static Spot reconstruct(UUID id, VehicleType type, SpotStatus status){
        return new Spot(id, type, status);
    }

    //Class constructor.
    private Spot(UUID id, VehicleType type, SpotStatus status) {

        validateData(id, type, status);

        this.id = id;
        this.type = type;
        this.status = status;
    }

    private void validateData(UUID id, VehicleType type, SpotStatus status){

        if (id == null) throw new InvalidSpotException("El id de la plaza no puede ser nulo");
        if (type == null) throw new InvalidSpotException("El tipo de vehículo de la plaza no puede ser nulo");
        if (status == null) throw new InvalidSpotException("El estado de la plaza no puede ser nulo");

    }


    // =============  STATE TRANSITIONS  ============= //
    public void occupy() {
        if (this.status != SpotStatus.AVAILABLE) {
            throw new InvalidSpotException("Solo se debe ocupar una plaza que esté DISPONIBLE");
        }
        this.status = SpotStatus.OCCUPIED;
    }

    public void release() {
        if (this.status != SpotStatus.OCCUPIED) {
            throw new InvalidSpotException("Solo se debe liberar una plaza que se encuentre ya OCUPADA");
        }
        this.status = SpotStatus.AVAILABLE;
    }

    public void blockForMaintenance() {
        if (this.status != SpotStatus.AVAILABLE) {
            throw new InvalidSpotException("Para poner una plaza en mantenimiento, debe estar DISPONIBLE");
        }
        this.status = SpotStatus.UNAVAILABLE;
    }

    public void unblock() {
        if (this.status != SpotStatus.UNAVAILABLE) {
            throw new InvalidSpotException("Solo se puede liberar una plaza que se encuentre NO DISPONIBLE");
        }
        this.status = SpotStatus.AVAILABLE;
    }


    // =============  GETTERS  ============= //
    public UUID getId() {
        return id;
    }

    public VehicleType getType() {
        return type;
    }

    public SpotStatus getStatus() {
        return status;
    }

}
