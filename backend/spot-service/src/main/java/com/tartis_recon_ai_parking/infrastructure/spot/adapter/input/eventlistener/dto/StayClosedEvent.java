package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.eventlistener.dto;

import java.time.Instant;
import java.util.UUID;

public record StayClosedEvent(
        UUID eventId,
        String type,
        String version,
        Instant occurredAt,
        StayClosedEventData data
) {
}
