package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.request;

import com.tartis_recon_ai_parking.domain.spot.VehicleType;

public class SpotRequest {

    private VehicleType type;
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
