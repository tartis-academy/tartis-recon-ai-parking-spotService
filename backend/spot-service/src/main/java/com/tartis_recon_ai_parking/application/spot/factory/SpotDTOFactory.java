package com.tartis_recon_ai_parking.application.spot.factory;

import com.tartis_recon_ai_parking.application.spot.dto.SpotDTO;
import com.tartis_recon_ai_parking.domain.spot.Spot;

public class SpotDTOFactory {

    private SpotDTOFactory() {
    }

    public static SpotDTO from(Spot spot) {
        return new SpotDTO(spot.getId(), spot.getType(), spot.getNumSpot(), spot.getStatus());
    }
}
