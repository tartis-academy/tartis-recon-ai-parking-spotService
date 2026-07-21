package com.tartis_recon_ai_parking.application.spot.port.output;

import com.tartis_recon_ai_parking.domain.spot.Spot;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpotPersistence {

    Spot save(Spot spot);

    Optional<Spot> findById(UUID id);

    List<Spot> findAll();
}
