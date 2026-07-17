package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.request;

import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SpotRequest {

    @NotNull(message = "El tipo de plaza es obligatorio")
    private VehicleType type;

    @NotNull(message = "El número de plaza es obligatorio")
    @Min(value = 1, message = "El número de plaza debe ser mayor que 0")
    private Integer numSpot;

    public SpotRequest() {
    }

    public SpotRequest(VehicleType type, Integer numSpot) {
        this.type = type;
        this.numSpot = numSpot;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public Integer getNumSpot() {
        return numSpot;
    }

    public void setNumSpot(Integer numSpot) {
        this.numSpot = numSpot;
    }
}
