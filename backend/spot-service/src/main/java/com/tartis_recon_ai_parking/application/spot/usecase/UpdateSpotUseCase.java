package com.tartis_recon_ai_parking.application.spot.usecase;

import com.tartis_recon_ai_parking.application.spot.dto.SpotCreateDTO;
import com.tartis_recon_ai_parking.application.spot.dto.SpotDTO;
import com.tartis_recon_ai_parking.application.spot.factory.SpotDTOFactory;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotFoundException;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class UpdateSpotUseCase {

    private final SpotPersistence spotPersistence;

    public UpdateSpotUseCase(SpotPersistence spotPersistence) {
        this.spotPersistence = spotPersistence;
    }

    // HU-05 CA4. Solo cambia el tipo; el estado tiene sus propios endpoints.
    @Transactional
    public SpotDTO execute(UUID id, SpotCreateDTO updateDTO) {
        Spot existing = spotPersistence.findById(id)
                .orElseThrow(() -> new SpotNotFoundException("No existe una plaza con id " + id));

        Spot updated = existing.changeTypeTo(updateDTO.getType());
        Spot saved = spotPersistence.save(updated);
        return SpotDTOFactory.from(saved);
    }
}
