package com.tartis_recon_ai_parking.application.spot.usecase;

import com.tartis_recon_ai_parking.application.spot.dto.SpotDTO;
import com.tartis_recon_ai_parking.application.spot.factory.SpotDTOFactory;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

public class GetSpotUseCase {

    private final SpotPersistence spotPersistence;

    public GetSpotUseCase(SpotPersistence spotPersistence) {
        this.spotPersistence = spotPersistence;
    }

    @Transactional(readOnly = true)
    public SpotDTO getById(UUID id) {
        return spotPersistence.findById(id)
                .map(SpotDTOFactory::from)
                .orElseThrow(() -> new SpotNotFoundException("No existe una plaza con id " + id));
    }

    @Transactional(readOnly = true)
    public List<SpotDTO> getAll() {
        return spotPersistence.findAll().stream()
                .map(SpotDTOFactory::from)
                .collect(Collectors.toList());
    }
}
