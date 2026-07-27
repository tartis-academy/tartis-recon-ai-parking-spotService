package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import jakarta.validation.constraints.NotNull;

public class SpotOccupyRequest {

    // "type" es el nombre que manda stay-service hoy. Se acepta como alias para
    // no acoplar el orden de despliegue; se retira cuando haya migrado.
    @NotNull(message = "vehicleType es obligatorio")
    @JsonAlias("type")
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