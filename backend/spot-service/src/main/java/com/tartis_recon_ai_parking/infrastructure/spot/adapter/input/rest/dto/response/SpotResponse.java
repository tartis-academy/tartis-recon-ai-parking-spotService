package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.response;

import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import java.util.UUID;

public class SpotResponse {

    private final UUID id;
    private final VehicleType type;
    private final SpotStatus status;

    public SpotResponse(UUID id, VehicleType type, SpotStatus status) {
        this.id = id;
        this.type = type;
        this.status = status;
    }

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
