package com.tartis_recon_ai_parking.application.spot.port.output;

import java.util.Optional;
import java.util.UUID;
import com.tartis_recon_ai_parking.domain.spot.Spot;

public interface SpotPersistence {

    /**
     * Busca una plaza por su identificador único.
     * Devuelve Optional porque la plaza podría no existir en la base de datos.
     */
    Optional<Spot> findById(UUID id);

    /**
     * Guarda una plaza (ya sea para crearla nueva o para actualizar su estado).
     * Devuelve la entidad Spot tal como quedó persistida.
     */
    Spot save(Spot spot);
}