package com.tartis_recon_ai_parking.infrastructure.spot.adapter.output.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import com.tartis_recon_ai_parking.domain.spot.VehicleType;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

import com.tartis_recon_ai_parking.domain.spot.SpotStatus;

public interface SpotRepository extends JpaRepository<SpotEntity, UUID> {
    long countByTypeAndStatus(VehicleType type, SpotStatus status);

    long countByType(VehicleType type);

    // timeout -2 = SKIP LOCKED.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("select s from SpotEntity s where s.type = :type and s.status = 'AVAILABLE'")
    List<SpotEntity> findAvailableForUpdate(VehicleType type, Limit limit);

    default Optional<SpotEntity> findFirstAvailable(VehicleType type) {
        return findAvailableForUpdate(type, Limit.of(1)).stream().findFirst();
    }
}
