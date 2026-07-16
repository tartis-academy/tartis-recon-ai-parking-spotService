package com.tartis_recon_ai_parking.application.spot.dto;

import com.tartis_recon_ai_parking.domain.spot.VehicleType;

public class SpotCreateDTO {

    private final VehicleType type;
    private final Integer numSpot;

    public SpotCreateDTO(VehicleType type, Integer numSpot) {
        this.type = type;
        this.numSpot = numSpot;
    }

    public VehicleType getType() {
        return type;
    }

    public Integer getNumSpot() {
        return numSpot;
    }
}