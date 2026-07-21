package com.tartis_recon_ai_parking.application.spot.usecase;

import com.tartis_recon_ai_parking.application.spot.dto.SpotCreateDTO;
import com.tartis_recon_ai_parking.application.spot.dto.SpotDTO;
import com.tartis_recon_ai_parking.application.spot.factory.SpotDTOFactory;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import java.util.UUID;

public class CreateSpotUseCase {

    private final SpotPersistence spotPersistence;

    public CreateSpotUseCase(SpotPersistence spotPersistence) {
        this.spotPersistence = spotPersistence;
    }

    public SpotDTO execute(SpotCreateDTO createDTO) {
        Spot spot = new Spot(UUID.randomUUID(), createDTO.getType(), createDTO.getNumSpot());
        Spot saved = spotPersistence.save(spot);
        return SpotDTOFactory.from(saved);
    }
}
