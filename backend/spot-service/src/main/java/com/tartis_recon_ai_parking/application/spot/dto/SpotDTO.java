package com.tartis_recon_ai_parking.application.spot.dto;

import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import java.util.UUID;

public class SpotDTO {

    private final UUID id;
    private final VehicleType type;
    private final Integer numSpot;
    private final SpotStatus status;

    public SpotDTO(UUID id, VehicleType type, Integer numSpot, SpotStatus status) {
        this.id = id;
        this.type = type;
        this.numSpot = numSpot;
        this.status = status;
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
}
