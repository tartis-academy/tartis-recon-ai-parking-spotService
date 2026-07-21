package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.response;

import com.tartis_recon_ai_parking.domain.spot.SpotStatus;

import java.util.UUID;

/**
 * Respuesta de POST /v1/spots/occupy segun el PDF (seccion 8.3):
 * { "spotId": "uuid", "numSpot": 12, "status": "OCCUPIED" }
 */
public class SpotOccupyResponse {

    private final UUID spotId;
    private final int numSpot;
    private final SpotStatus status;

    public SpotOccupyResponse(UUID spotId, int numSpot, SpotStatus status) {
        this.spotId = spotId;
        this.numSpot = numSpot;
        this.status = status;
    }

    public UUID getSpotId() {
        return spotId;
    }

    public int getNumSpot() {
        return numSpot;
    }

    public SpotStatus getStatus() {
        return status;
    }
}
