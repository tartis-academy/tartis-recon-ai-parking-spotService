package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest;

import com.tartis_recon_ai_parking.application.spot.dto.SpotCreateDTO;
import com.tartis_recon_ai_parking.application.spot.dto.SpotDTO;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.request.SpotRequest;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.response.SpotResponse;

public class SpotRestMapper {

    private SpotRestMapper() {
    }

    public static SpotCreateDTO toCreateDTO(SpotRequest request) {
        return new SpotCreateDTO(request.getType(), request.getNumSpot());
    }

    public static SpotResponse toResponse(SpotDTO dto) {
        return new SpotResponse(dto.getId(), dto.getType(), dto.getNumSpot(), dto.getStatus());
    }
}
