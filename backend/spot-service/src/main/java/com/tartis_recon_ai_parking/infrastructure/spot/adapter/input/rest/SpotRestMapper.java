package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest;

import org.springframework.stereotype.Component;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.response.SpotResponse;

@Component
public class SpotRestMapper {


    public SpotResponse toResponse(Spot spot) {
        if (spot == null) {
            return null;
        }

        return new SpotResponse(
            spot.getId(),
            spot.getNumSpot(),
            spot.getType(),
            spot.getStatus()
        );
    }
}