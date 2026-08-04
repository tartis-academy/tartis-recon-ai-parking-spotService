package com.tartis_recon_ai_parking.application.spot.dto;

import java.time.Instant;
import java.util.UUID;

import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;

public record SpotStatusChangedEvent(
    UUID eventId,
    String type,
    String version,
    Instant occurredAt,
    SpotStatusChangedData data
) {
    public record SpotStatusChangedData(
        UUID spotId,
        VehicleType vehicleType,
        SpotStatus status
    ) {}

    public static SpotStatusChangedEvent of(Spot spot, Instant occurredAt) {
        return new SpotStatusChangedEvent(
            UUID.randomUUID(), "SpotStatusChangedEvent", "v1", occurredAt,
            new SpotStatusChangedData(spot.getId(), spot.getType(), spot.getStatus())
        );
    }
}
