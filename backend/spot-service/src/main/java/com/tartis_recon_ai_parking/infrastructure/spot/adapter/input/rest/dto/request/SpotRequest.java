package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.request;

import com.tartis_recon_ai_parking.domain.spot.VehicleType;

public class SpotRequest {

    private VehicleType type;

    public SpotRequest() {
    }

    public SpotRequest(VehicleType type) {
        this.type = type;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

}
