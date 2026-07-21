package com.tartis_recon_ai_parking.application.spot.port.output;

import com.tartis_recon_ai_parking.domain.spot.Spot;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;

/**
 * Puerto de salida para la persistencia de Spot.
 * Alcance: HU-05 (Gestionar plazas) - CRUD por ID desde el menu Administrador.
 */
public interface SpotPersistence {

    Spot save(Spot spot);

    Optional<Spot> findById(UUID id);

    List<Spot> findAll();

    boolean existsById(UUID id);

    long countByTypeAndStatus(VehicleType type, SpotStatus status);
}
