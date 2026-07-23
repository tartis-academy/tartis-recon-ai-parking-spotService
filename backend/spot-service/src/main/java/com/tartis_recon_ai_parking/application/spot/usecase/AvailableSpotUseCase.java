package com.tartis_recon_ai_parking.application.spot.usecase;

import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;

public class AvailableSpotUseCase {

  private final SpotPersistence persistence;

    public AvailableSpotUseCase(SpotPersistence persistence) {
        this.persistence = persistence;
    }

public boolean execute(VehicleType type) {
   return persistence.existsAvailableByType(type);
    
}
}