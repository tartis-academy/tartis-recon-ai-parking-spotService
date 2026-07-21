package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tartis_recon_ai_parking.application.spot.dto.SpotCreateDTO;
import com.tartis_recon_ai_parking.application.spot.dto.SpotDTO;
import com.tartis_recon_ai_parking.application.spot.usecase.UpdateSpotUseCase;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.request.SpotRequest;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.response.SpotResponse;

@RestController
@RequestMapping("/v1/spots")
public class SpotRestAdapter {

    private final UpdateSpotUseCase updateSpotStatusUseCase;
    private final SpotRestMapper spotRestMapper;

    public SpotRestAdapter(UpdateSpotUseCase updateSpotStatusUseCase,
                            SpotRestMapper spotRestMapper) {
        this.updateSpotStatusUseCase = updateSpotStatusUseCase;
        this.spotRestMapper = spotRestMapper;
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SpotResponse> updateStatus(@PathVariable UUID id,
                                                       @RequestBody SpotRequest request) {
                                                        
        SpotCreateDTO create = new SpotCreateDTO(request.getType()); 
        SpotDTO updatedSpot = updateSpotStatusUseCase.execute(id, create);
        SpotResponse response = spotRestMapper.toResponse(updatedSpot);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}