package com.tartis_recon_ai_parking.application.spot.usecase;

import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import org.springframework.transaction.annotation.Transactional;

public class AvailableSpotUseCase {

  private final SpotPersistence persistence;

    public AvailableSpotUseCase(SpotPersistence persistence) {
        this.persistence = persistence;
    }

@Transactional(readOnly = true)
public boolean execute(VehicleType type) {
   return persistence.existsAvailableByType(type);
    
}
}