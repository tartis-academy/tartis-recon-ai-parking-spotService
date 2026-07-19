package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest;

import java.util.UUID;

import com.tartis_recon_ai_parking.application.spot.dto.SpotDTO;
import com.tartis_recon_ai_parking.application.spot.usecase.CreateSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.GetSpotUseCase;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.response.SpotResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tartis_recon_ai_parking.application.spot.usecase.ReleaseSpotUseCase;

@RestController
@RequestMapping("/v1/spots")
public class SpotRestAdapter {

    private final ReleaseSpotUseCase releaseSpotUseCase;

public SpotRestAdapter(ReleaseSpotUseCase releaseSpotUseCase) {
    this.releaseSpotUseCase = releaseSpotUseCase;
}

@PostMapping("/{id}/release")
    public ResponseEntity<SpotResponse> release(@PathVariable UUID id) {
        SpotDTO released = releaseSpotUseCase.execute(id);
        return ResponseEntity.ok(SpotRestMapper.toResponse(released));
}

}
