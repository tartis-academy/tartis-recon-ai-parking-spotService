package com.tartis_recon_ai_parking.application.spot.usecase;

import java.util.UUID;

import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotFoundException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotValidationException;
import com.tartis_recon_ai_parking.domain.spot.exception.UnsupportedSpotStatusTransitionException;

public class UpdateSpotStatusUseCase {

    private final SpotPersistence spotPersistence;

    public UpdateSpotStatusUseCase(SpotPersistence spotPersistence) {
        this.spotPersistence = spotPersistence;
    }

    /**
     * Solo admite transiciones AVAILABLE <-> UNAVAILABLE. Ocupar y liberar
     * tienen sus propios endpoints (/spots/occupy y /spots/{id}/release).
     */
    public Spot execute(UUID id, SpotStatus newStatus) {
        if (newStatus == null) {
            throw new SpotValidationException("El estado solicitado no puede ser nulo");
        }
        if (newStatus == SpotStatus.OCCUPIED) {
            throw new UnsupportedSpotStatusTransitionException(
                    "OCCUPIED no se puede solicitar por este endpoint; usa POST /spots/occupy.");
        }

        Spot spot = spotPersistence.findById(id)
                .orElseThrow(() -> new SpotNotFoundException("No se encontró ninguna plaza con el ID: " + id));

        if (spot.getStatus() == newStatus) {
            return spot;
        }

        if (newStatus == SpotStatus.UNAVAILABLE) {
            spot.blockForMaintenance();
        } else {
            spot.unblock();
        }

        return spotPersistence.save(spot);
    }
}
