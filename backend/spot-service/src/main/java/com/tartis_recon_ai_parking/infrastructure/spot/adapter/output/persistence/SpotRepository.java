package com.tartis_recon_ai_parking.infrastructure.spot.adapter.output.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;

public interface SpotRepository extends JpaRepository<SpotEntity, UUID> {
    long countByTypeAndStatus(VehicleType type, SpotStatus status);
}