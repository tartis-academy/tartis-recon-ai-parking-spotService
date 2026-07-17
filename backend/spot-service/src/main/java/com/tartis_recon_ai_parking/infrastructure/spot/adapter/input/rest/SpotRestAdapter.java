package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest;

import com.tartis_recon_ai_parking.application.spot.dto.SpotDTO;
import com.tartis_recon_ai_parking.application.spot.factory.SpotDTOFactory;
import com.tartis_recon_ai_parking.application.spot.usecase.CreateSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.GetSpotUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.UpdateSpotStatusUseCase;
import com.tartis_recon_ai_parking.application.spot.usecase.UpdateSpotUseCase;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.request.SpotRequest;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.request.SpotStatusRequest;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.rest.dto.response.SpotResponse;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/spots")
public class SpotRestAdapter {

    private final CreateSpotUseCase createSpotUseCase;
    private final GetSpotUseCase getSpotUseCase;
    private final UpdateSpotUseCase updateSpotUseCase;
    private final UpdateSpotStatusUseCase updateSpotStatusUseCase;

    public SpotRestAdapter(CreateSpotUseCase createSpotUseCase,
                            GetSpotUseCase getSpotUseCase,
                            UpdateSpotUseCase updateSpotUseCase,
                            UpdateSpotStatusUseCase updateSpotStatusUseCase) {
        this.createSpotUseCase = createSpotUseCase;
        this.getSpotUseCase = getSpotUseCase;
        this.updateSpotUseCase = updateSpotUseCase;
        this.updateSpotStatusUseCase = updateSpotStatusUseCase;
    }

    @PostMapping
    public ResponseEntity<SpotResponse> create(@Valid @RequestBody SpotRequest request) {
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
    public ResponseEntity<SpotResponse> update(@PathVariable UUID id, @Valid @RequestBody SpotRequest request) {
        SpotDTO updated = updateSpotUseCase.execute(id, SpotRestMapper.toCreateDTO(request));
        return ResponseEntity.ok(SpotRestMapper.toResponse(updated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SpotResponse> updateStatus(@PathVariable UUID id, @Valid @RequestBody SpotStatusRequest request) {
        Spot updated = updateSpotStatusUseCase.execute(id, request.getStatus());
        return ResponseEntity.ok(SpotRestMapper.toResponse(SpotDTOFactory.from(updated)));
    }
}
