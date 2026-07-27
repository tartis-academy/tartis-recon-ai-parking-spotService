package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest;

import org.springframework.stereotype.Component;

import com.tartis_recon_ai_parking.application.spot.dto.SpotAvailabilityDTO;
import com.tartis_recon_ai_parking.application.spot.dto.SpotDTO;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.response.AvailabilityResponse;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.response.SpotResponse;

@Component
public class SpotRestMapper {


    public SpotResponse toResponse(SpotDTO spot) {
        if (spot == null) {
            return null;
        }

        return new SpotResponse(
            spot.getId(),
            spot.getType(),
            spot.getStatus()
        );
    }

    public AvailabilityResponse toResponse(SpotAvailabilityDTO availability) {
        if (availability == null) {
            return null;
        }

        return new AvailabilityResponse(
            availability.getType(),
            availability.isAvailable(),
            availability.getAvailableCount(),
            availability.getTotalCount()
        );
    }
}