package com.tartis_recon_ai_parking.application.spot.usecase;

import com.tartis_recon_ai_parking.application.spot.dto.SpotAvailabilityDTO;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import org.springframework.transaction.annotation.Transactional;

public class AvailableSpotUseCase {

    private final SpotPersistence persistence;

    public AvailableSpotUseCase(SpotPersistence persistence) {
        this.persistence = persistence;
    }

    // HU-03. Solo cuenta AVAILABLE, asi que las UNAVAILABLE nunca se ofrecen (RN-10).
    @Transactional(readOnly = true)
    public SpotAvailabilityDTO execute(VehicleType type) {
        long available = persistence.countByTypeAndStatus(type, SpotStatus.AVAILABLE);
        long total = persistence.countByType(type);
        return new SpotAvailabilityDTO(type, available, total);
    }
}
