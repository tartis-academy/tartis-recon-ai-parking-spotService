package com.tartis_recon_ai_parking.application.spot.usecase;

import com.tartis_recon_ai_parking.application.spot.dto.SpotDTO;
import com.tartis_recon_ai_parking.application.spot.factory.SpotDTOFactory;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotFoundException;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class ReleaseSpotUseCase {

    private final SpotPersistence spotPersistence;

    public ReleaseSpotUseCase(SpotPersistence spotPersistence) {
        this.spotPersistence = spotPersistence;
    }

    @Transactional
    public SpotDTO execute(UUID id) {
        return execute(id, null);
    }

    public SpotDTO execute(UUID id, java.time.Instant eventOccurredAt) {
        Spot spot = spotPersistence.findById(id)
                .orElseThrow(() -> new SpotNotFoundException("No existe una plaza con id " + id));

        if (eventOccurredAt != null && spot.getLastStatusChangeAt() != null 
                && eventOccurredAt.isBefore(spot.getLastStatusChangeAt())) {
            throw new com.tartis_recon_ai_parking.domain.spot.exception.SpotEventOutdatedException(
                    "El evento es de " + eventOccurredAt + " pero la plaza cambió de estado por última vez en " + spot.getLastStatusChangeAt()
            );
        }

        spot.release(); 

        Spot saved = spotPersistence.save(spot);
        return SpotDTOFactory.from(saved);
    }
}