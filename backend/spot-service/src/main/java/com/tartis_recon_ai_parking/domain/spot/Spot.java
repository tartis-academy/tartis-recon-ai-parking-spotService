package com.tartis_recon_ai_parking.domain.spot;

import com.tartis_recon_ai_parking.domain.spot.exception.SpotAlreadyOccupiedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotCannotBeBlockedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotAvailableException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotBlockedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotOccupiedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotTypeChangeNotAllowedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotValidationException;
import java.util.UUID;

public final class Spot {

    private final UUID id;
    private final VehicleType type;
    private SpotStatus status;

    //Creates a new spot with its own ID.
    public static Spot create(VehicleType type){
        return new Spot(UUID.randomUUID(), type, SpotStatus.AVAILABLE);
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

        if (id == null) throw new SpotValidationException("El id de la plaza no puede ser nulo");
        if (type == null) throw new SpotValidationException("El tipo de vehículo de la plaza no puede ser nulo");
        if (status == null) throw new SpotValidationException("El estado de la plaza no puede ser nulo");

    }


    // =============  STATE TRANSITIONS  ============= //
    public void occupy() {
        if (this.status == SpotStatus.OCCUPIED) {
            throw new SpotAlreadyOccupiedException("Solo se debe ocupar una plaza que se encuentre DISPONIBLE (la plaza ya está ocupada)");
        }
        if (this.status == SpotStatus.UNAVAILABLE) {
            throw new SpotNotAvailableException("No se puede ocupar una plaza que se encuentra NO DISPONIBLE (en mantenimiento)");
        }
        this.status = SpotStatus.OCCUPIED;
    }

    public void release() {
        if (this.status != SpotStatus.OCCUPIED) {
            throw new SpotNotOccupiedException("Solo se debe liberar una plaza que se encuentre ya OCUPADA");
        }
        this.status = SpotStatus.AVAILABLE;
    }

    public void blockForMaintenance() {
        if (this.status != SpotStatus.AVAILABLE) {
            throw new SpotCannotBeBlockedException("Para poner una plaza en mantenimiento, debe estar DISPONIBLE");
        }
        this.status = SpotStatus.UNAVAILABLE;
    }

    public void unblock() {
        if (this.status != SpotStatus.UNAVAILABLE) {
            throw new SpotNotBlockedException("Solo se puede liberar una plaza que se encuentre NO DISPONIBLE");
        }
        this.status = SpotStatus.AVAILABLE;
    }

    // IN-05/IN-18: si la plaza esta OCUPADA hay una estancia en curso asociada y
    // el tipo no puede cambiar. El tipo es final, por eso devuelve una plaza nueva.
    public Spot changeTypeTo(VehicleType newType) {
        if (this.status == SpotStatus.OCCUPIED) {
            throw new SpotTypeChangeNotAllowedException(
                    "No se puede cambiar el tipo de una plaza OCUPADA: hay una estancia en curso asociada (IN-05/IN-18)");
        }
        return new Spot(this.id, newType, this.status);
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