package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.eventlistener.dto;

import java.util.UUID;

/**
 * Tolerant reader: Solo extraemos el spotId porque es lo único
 * que necesita el spot-service para liberar la plaza.
 * Ignoramos el exitDate, el importe total, etc.
 */
public record StayClosedEventData(
        UUID spotId
) {
}
