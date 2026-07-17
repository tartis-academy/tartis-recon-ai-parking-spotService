package com.tartis_recon_ai_parking.infrastructure.spot.adapter.output.persistence;


import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;


public interface SpotRepository extends JpaRepository<SpotEntity, UUID> {
}
