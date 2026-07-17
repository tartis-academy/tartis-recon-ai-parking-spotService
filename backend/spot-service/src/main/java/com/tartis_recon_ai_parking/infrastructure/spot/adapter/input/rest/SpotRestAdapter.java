package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest;

import com.tartis_recon_ai_parking.application.spot.dto.SpotDTO;
import com.tartis_recon_ai_parking.application.spot.usecase.CreateSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.GetSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.UpdateSpotUseCase;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.request.SpotRequest;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.response.SpotResponse;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/spots")
public class SpotRestAdapter {

    private final CreateSpotUseCase createSpotUseCase;
    private final GetSpotUseCase getSpotUseCase;
    private final UpdateSpotUseCase updateSpotUseCase;

    public SpotRestAdapter(CreateSpotUseCase createSpotUseCase,
                            GetSpotUseCase getSpotUseCase,
                            UpdateSpotUseCase updateSpotUseCase) {
        this.createSpotUseCase = createSpotUseCase;
        this.getSpotUseCase = getSpotUseCase;
        this.updateSpotUseCase = updateSpotUseCase;
    }

    @PostMapping
    public ResponseEntity<SpotResponse> create(@RequestBody SpotRequest request) {
        SpotDTO created = createSpotUseCase.execute(SpotRestMapper.toCreateDTO(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(SpotRestMapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<SpotResponse>> getAll() {
        List<SpotResponse> spots = getSpotUseCase.getAll().stream()
                .map(SpotRestMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(spots);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpotResponse> getById(@PathVariable UUID id) {
        SpotDTO spot = getSpotUseCase.getById(id);
        return ResponseEntity.ok(SpotRestMapper.toResponse(spot));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SpotResponse> update(@PathVariable UUID id, @RequestBody SpotRequest request) {
        SpotDTO updated = updateSpotUseCase.execute(id, SpotRestMapper.toCreateDTO(request));
        return ResponseEntity.ok(SpotRestMapper.toResponse(updated));
    }
}