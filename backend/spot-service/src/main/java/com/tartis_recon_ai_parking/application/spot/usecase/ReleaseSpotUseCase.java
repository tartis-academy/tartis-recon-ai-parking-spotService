package com.tartis_recon_ai_parking.application.spot.usecase;

import com.tartis_recon_ai_parking.application.spot.dto.SpotDTO;
import com.tartis_recon_ai_parking.application.spot.factory.SpotDTOFactory;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotEventOutdatedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotFoundException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ReleaseSpotUseCase {

    private final SpotPersistence spotPersistence;

    public ReleaseSpotUseCase(SpotPersistence spotPersistence) {
        this.spotPersistence = spotPersistence;
    }

    public SpotDTO execute(UUID id) {
        return execute(id, null);
    }

    public SpotDTO execute(UUID id, Instant eventOccurredAt) {
        Spot spot = spotPersistence.findById(id)
                .orElseThrow(() -> new SpotNotFoundException("No existe una plaza con id " + id));

        /**
         * Guard de idempotencia por timestamp:
         * Compara la fecha de ocurrencia del evento externo con la fecha del último cambio registrado en la plaza.
         * 
         * NOTA DE ARQUITECTURA: Esta guardia asume que los relojes de los microservicios emisores (stay-service)
         * y receptores (spot-service) se encuentran sincronizados vía NTP. Si existiera descalibración horaria (clock skew),
         * eventos válidos con timestamps desfasados podrían ser descartados.
         */
        if (eventOccurredAt != null && spot.getLastStatusChangeAt() != null 
                && eventOccurredAt.isBefore(spot.getLastStatusChangeAt())) {
            throw new SpotEventOutdatedException(
                    "La plaza " + id + " cambió de estado por última vez en " + spot.getLastStatusChangeAt() + " pero el evento es de " + eventOccurredAt
            );
        }

        spot.release(); 

        Spot saved = spotPersistence.save(spot);
        return SpotDTOFactory.from(saved);
    }
}