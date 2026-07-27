package com.tartis_recon_ai_parking.application.spot.dto;

import com.tartis_recon_ai_parking.domain.spot.VehicleType;

public class SpotCreateDTO {

    private final VehicleType type;

    public SpotCreateDTO(VehicleType type) {
        this.type = type;
    }

    public VehicleType getType() {
        return type;
    }
}