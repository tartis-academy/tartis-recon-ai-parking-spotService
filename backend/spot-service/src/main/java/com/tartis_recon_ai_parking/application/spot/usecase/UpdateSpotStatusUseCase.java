package com.tartis_recon_ai_parking.application.spot.usecase;

import java.util.UUID;

import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotCannotBeBlockedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotFoundException;

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
        Spot spot = spotPersistence.findById(id)
                .orElseThrow(() -> new SpotNotFoundException("No se encontró ninguna plaza con el ID: " + id));

        if (newStatus == SpotStatus.OCCUPIED) {
            throw new SpotCannotBeBlockedException("OCCUPIED no se puede solicitar por este endpoint; usa POST /spots/occupy.");
        }
        if (spot.getStatus() == SpotStatus.OCCUPIED) {
            throw new SpotCannotBeBlockedException("No se puede cambiar el estado de una plaza OCCUPIED; usa POST /spots/{id}/release.");
        }

        if (newStatus == SpotStatus.UNAVAILABLE && spot.getStatus() == SpotStatus.AVAILABLE) {
            spot.blockForMaintenance();
        } else if (newStatus == SpotStatus.AVAILABLE && spot.getStatus() == SpotStatus.UNAVAILABLE) {
            spot.unblock();
        }
        // Mismo estado solicitado: no-op idempotente, se guarda sin cambios.

        return spotPersistence.save(spot);
    }
}
