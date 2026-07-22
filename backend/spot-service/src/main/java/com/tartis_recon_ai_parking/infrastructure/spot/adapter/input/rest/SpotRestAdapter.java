package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tartis_recon_ai_parking.application.spot.dto.SpotCreateDTO;
import com.tartis_recon_ai_parking.application.spot.dto.SpotDTO;
import com.tartis_recon_ai_parking.application.spot.usecase.AvailableSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.CreateSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.GetSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.UpdateSpotUseCase;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import com.tartis_recon_ai_parking.application.spot.usecase.CreateSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.GetSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.UpdateSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.OccupySpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.ReleaseSpotUseCase;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.request.SpotRequest;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.response.SpotResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/spots")
public class SpotRestAdapter {

    private final CreateSpotUseCase createSpotUseCase;
    private final GetSpotUseCase getSpotUseCase;
    private final UpdateSpotUseCase updateSpotUseCase;
    private final OccupySpotUseCase occupySpotUseCase;
    private final ReleaseSpotUseCase releaseSpotUseCase;
    private final UpdateSpotUseCase updateSpotStatusUseCase;
    private final SpotRestMapper spotRestMapper;
    private final AvailableSpotUseCase availableSpotUseCase;

    public SpotRestAdapter(CreateSpotUseCase createSpotUseCase,
            GetSpotUseCase getSpotUseCase,
            UpdateSpotUseCase updateSpotUseCase,
            OccupySpotUseCase occupySpotUseCase,
            ReleaseSpotUseCase releaseSpotUseCase,
            UpdateSpotUseCase updateSpotStatusUseCase,
            SpotRestMapper spotRestMapper,
        AvailableSpotUseCase availableSpotUseCase) {
        this.createSpotUseCase = createSpotUseCase;
        this.getSpotUseCase = getSpotUseCase;
        this.updateSpotUseCase = updateSpotUseCase;
        this.occupySpotUseCase = occupySpotUseCase;
        this.releaseSpotUseCase = releaseSpotUseCase;
        this.updateSpotStatusUseCase = updateSpotStatusUseCase;
        this.spotRestMapper = spotRestMapper;
        this.availableSpotUseCase = availableSpotUseCase;
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SpotResponse> updateStatus(@PathVariable UUID id,
            @RequestBody SpotRequest request) {

        SpotCreateDTO create = new SpotCreateDTO(request.getType());
        SpotDTO updatedSpot = updateSpotStatusUseCase.execute(id, create);
        SpotResponse response = spotRestMapper.toResponse(updatedSpot);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    public ResponseEntity<SpotResponse> create(@Valid @RequestBody SpotRequest request) {
        SpotCreateDTO createDTO = new SpotCreateDTO(request.getType());
        SpotDTO created = createSpotUseCase.execute(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(spotRestMapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<SpotResponse>> getAll() {
        List<SpotResponse> spots = getSpotUseCase.getAll().stream()
                .map(spotRestMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(spots);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpotResponse> getById(@PathVariable UUID id) {
        SpotDTO spot = getSpotUseCase.getById(id);
        return ResponseEntity.ok(spotRestMapper.toResponse(spot));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SpotResponse> update(@PathVariable UUID id, @Valid @RequestBody SpotRequest request) {
        SpotCreateDTO createDTO = new SpotCreateDTO(request.getType());
        SpotDTO updated = updateSpotUseCase.execute(id, createDTO);
        return ResponseEntity.ok(spotRestMapper.toResponse(updated));
    }

    @PostMapping("/occupy")
    public ResponseEntity<SpotResponse> occupy(@Valid @RequestBody SpotRequest request) {
        com.tartis_recon_ai_parking.domain.spot.Spot occupied = occupySpotUseCase.execute(request.getType());
        return ResponseEntity.ok(spotRestMapper
                .toResponse(com.tartis_recon_ai_parking.application.spot.factory.SpotDTOFactory.from(occupied)));
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<SpotResponse> release(@PathVariable UUID id) {
        SpotDTO released = releaseSpotUseCase.execute(id);
        return ResponseEntity.ok(spotRestMapper.toResponse(released));
    }

    @GetMapping("/available")
    public ResponseEntity<Boolean> checkAvailability(@RequestParam VehicleType type) {
        boolean isAvailable = availableSpotUseCase.execute(type);
        return ResponseEntity.ok(isAvailable);
    }
}