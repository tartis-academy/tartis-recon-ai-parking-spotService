package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.request;

import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import jakarta.validation.constraints.NotNull;

public class SpotStatusRequest {

    @NotNull(message = "El estado de la plaza es obligatorio")
    private SpotStatus status;

    public SpotStatusRequest() {
    }

    public SpotStatusRequest(SpotStatus status) {
        this.status = status;
    }

    public SpotStatus getStatus() {
        return status;
    }

    public void setStatus(SpotStatus status) {
        this.status = status;
    }
}
