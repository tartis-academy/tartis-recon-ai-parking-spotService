package com.tartis_recon_ai_parking.application.spot.usecase;

import com.tartis_recon_ai_parking.application.spot.dto.SpotCreateDTO;
import com.tartis_recon_ai_parking.application.spot.dto.SpotDTO;
import com.tartis_recon_ai_parking.application.spot.factory.SpotDTOFactory;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotFoundException;
import java.util.UUID;

public class UpdateSpotUseCase {

    private final SpotPersistence spotPersistence;

    public UpdateSpotUseCase(SpotPersistence spotPersistence) {
        this.spotPersistence = spotPersistence;
    }

    public SpotDTO execute(UUID id, SpotCreateDTO updateDTO) {
        Spot existing = spotPersistence.findById(id)
                .orElseThrow(() -> new SpotNotFoundException("No existe una plaza con id " + id));

        Spot updated = Spot.reconstruct(id, updateDTO.getType(), existing.getStatus());
        Spot saved = spotPersistence.save(updated);
        return SpotDTOFactory.from(saved);
    }
}
