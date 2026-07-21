package com.tartis_recon_ai_parking.infrastructure.spot.adapter.output.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.tartis_recon_ai_parking.domain.spot.VehicleType;

import jakarta.persistence.LockModeType;

import com.tartis_recon_ai_parking.domain.spot.SpotStatus;

public interface SpotRepository extends JpaRepository<SpotEntity, UUID> {
    long countByTypeAndStatus(VehicleType type, SpotStatus status);

     @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SpotEntity s where s.type = :type and s.status = 'AVAILABLE' order by s.numSpot asc")
    List<SpotEntity> findAvailableForUpdate(VehicleType type);
    default Optional<SpotEntity> findFirstAvailable(VehicleType type) {
        return findAvailableForUpdate(type).stream().findFirst();
    }
}