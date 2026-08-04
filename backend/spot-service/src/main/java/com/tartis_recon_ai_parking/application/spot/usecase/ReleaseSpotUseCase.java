package com.tartis_recon_ai_parking.application.spot.usecase;

import com.tartis_recon_ai_parking.application.spot.dto.SpotDTO;
import com.tartis_recon_ai_parking.application.spot.dto.SpotStatusChangedEvent;
import com.tartis_recon_ai_parking.application.spot.factory.SpotDTOFactory;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotEventPublisher;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotEventOutdatedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotFoundException;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ReleaseSpotUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReleaseSpotUseCase.class);

    private final SpotPersistence spotPersistence;
    private final SpotEventPublisher eventPublisher;

    public ReleaseSpotUseCase(SpotPersistence spotPersistence, SpotEventPublisher eventPublisher) {
        this.spotPersistence = spotPersistence;
        this.eventPublisher = eventPublisher;
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
        publishSpotStatusChangedEventQuietly(saved);
        return SpotDTOFactory.from(saved);
    }

    private void publishSpotStatusChangedEventQuietly(Spot spot) {
        try {
            eventPublisher.publish(SpotStatusChangedEvent.of(spot, Instant.now()));
        } catch (RuntimeException e) {
            log.error("No se pudo publicar el evento de cambio de estado para la plaza {}", spot.getId(), e);
        }
    }
}
