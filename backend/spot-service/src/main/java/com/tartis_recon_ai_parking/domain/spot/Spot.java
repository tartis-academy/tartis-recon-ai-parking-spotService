package com.tartis_recon_ai_parking.domain.spot;

import com.tartis_recon_ai_parking.domain.spot.exception.InvalidSpotException;
import java.util.Objects;
import java.util.UUID;

public final class Spot {

    private final UUID id;
    private final VehicleType type;
    private final Integer numSpot;
    private SpotStatus status;

    public Spot(UUID id, VehicleType type, Integer numSpot) {
        validateId(id);
        validateType(type);
        validateNumSpot(numSpot);

        this.id = id;
        this.type = type;
        this.numSpot = numSpot;
        this.status = SpotStatus.AVAILABLE;
    }

    public Spot(UUID id, VehicleType type, Integer numSpot, SpotStatus status) {
        validateId(id);
        validateType(type);
        validateNumSpot(numSpot);
        validateStatus(status);

        this.id = id;
        this.type = type;
        this.numSpot = numSpot;
        this.status = status;
    }

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

    private void validateId(UUID id) {
        if (id == null) {
            throw new InvalidSpotException("El id de la plaza no puede ser nulo");
        }
    }

    private void validateType(VehicleType type) {
        if (type == null) {
            throw new InvalidSpotException("El tipo de vehículo de la plaza no puede ser nulo");
        }
    }

    private void validateNumSpot(Integer numSpot) {
        if (numSpot == null || numSpot <= 0) {
            throw new InvalidSpotException("El número de plaza debe ser mayor que 0");
        }
    }

    private void validateStatus(SpotStatus status) {
        if (status == null) {
            throw new InvalidSpotException("El estado de la plaza no puede ser nulo");
        }
    }

    public UUID getId() {
        return id;
    }

    public VehicleType getType() {
        return type;
    }

    public Integer getNumSpot() {
        return numSpot;
    }

    public SpotStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Spot)) return false;
        Spot spot = (Spot) o;
        return id.equals(spot.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Spot{" +
                "id=" + id +
                ", type=" + type +
                ", numSpot=" + numSpot +
                ", status=" + status +
                '}';
    }

}

