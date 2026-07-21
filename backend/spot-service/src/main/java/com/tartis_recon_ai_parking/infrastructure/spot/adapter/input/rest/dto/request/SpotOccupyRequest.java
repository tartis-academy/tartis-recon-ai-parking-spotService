package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.request;

import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import jakarta.validation.constraints.NotNull;

public class SpotOccupyRequest {

    @NotNull(message = "vehicleType es obligatorio")
    private VehicleType vehicleType;

    public SpotOccupyRequest() {
        // Requerido por Jackson.
    }

    public SpotOccupyRequest(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }
}