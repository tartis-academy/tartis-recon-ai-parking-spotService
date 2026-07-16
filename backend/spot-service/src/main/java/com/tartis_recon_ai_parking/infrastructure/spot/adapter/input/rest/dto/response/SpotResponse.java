package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.response;

import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import java.util.UUID;

public class SpotResponse {
    private UUID id;
    private int numSpot;
    private String type;
    private SpotStatus status;

    public SpotResponse(UUID id, int numSpot, String type, SpotStatus status) {
        this.id = id;
        this.numSpot = numSpot;
        this.type = type;
        this.status = status;
    }

    // Getters necesarios para que Jackson pueda generar el JSON
    public UUID getId() { return id; }
    public int getNumSpot() { return numSpot; }
    public String getType() { return type; }
    public SpotStatus getStatus() { return status; }
}