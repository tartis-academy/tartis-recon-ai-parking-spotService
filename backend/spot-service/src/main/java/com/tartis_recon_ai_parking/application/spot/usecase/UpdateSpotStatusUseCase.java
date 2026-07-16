package com.tartis_recon_ai_parking.application.spot.usecase;

import java.util.UUID;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.exception.InvalidSpotException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotFoundException;

public class UpdateSpotStatusUseCase {

    private final SpotPersistence spotPersistence;

    public UpdateSpotStatusUseCase(SpotPersistence spotPersistence) {
        this.spotPersistence = spotPersistence;
    }

    public Spot execute(UUID id, SpotStatus newStatus) {
        // 1. Recuperamos el Spot
        Spot spot = spotPersistence.findById(id)
            .orElseThrow(() -> new SpotNotFoundException("No se encontró ninguna plaza con el ID: " + id));

        // 2. VALIDACIÓN EN EL CASO DE USO
        // Comprobamos la regla de negocio antes de intentar modificar el estado
        if (spot.getStatus() == SpotStatus.OCCUPIED && newStatus == SpotStatus.UNAVAILABLE) {
            throw new InvalidSpotException("No se puede inhabilitar una plaza de parking que actualmente está ocupada.");
        }

        // 3. Actualizamos el estado usando el setter estándar (asumiendo que tu compañero lo creó)
        spot.setStatus(newStatus);

        // 4. Persistimos y retornamos
        return spotPersistence.save(spot);
    }
}