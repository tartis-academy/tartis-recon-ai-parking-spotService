package com.tartis_recon_ai_parking.application.spot.usecase;

import java.time.Instant;
import java.util.UUID;

import com.tartis_recon_ai_parking.application.spot.dto.SpotStatusChangedEvent;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotEventPublisher;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotFoundException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotValidationException;
import com.tartis_recon_ai_parking.domain.spot.exception.UnsupportedSpotStatusTransitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class UpdateSpotStatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateSpotStatusUseCase.class);

    private final SpotPersistence spotPersistence;
    private final SpotEventPublisher eventPublisher;

    public UpdateSpotStatusUseCase(SpotPersistence spotPersistence, SpotEventPublisher eventPublisher) {
        this.spotPersistence = spotPersistence;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Solo admite transiciones AVAILABLE <-> UNAVAILABLE. Ocupar y liberar
     * tienen sus propios endpoints (/spots/occupy y /spots/{id}/release).
     */
    @Transactional
    public Spot execute(UUID id, SpotStatus newStatus) {
        if (newStatus == null) {
            throw new SpotValidationException("El estado solicitado no puede ser nulo");
        }
        if (newStatus == SpotStatus.OCCUPIED) {
            throw new UnsupportedSpotStatusTransitionException(
                    "OCCUPIED no se puede solicitar por este endpoint; usa POST /spots/occupy.");
        }

        Spot spot = spotPersistence.findById(id)
                .orElseThrow(() -> new SpotNotFoundException("No se encontró ninguna plaza con el ID: " + id));

        if (spot.getStatus() == newStatus) {
            return spot;
        }

        if (newStatus == SpotStatus.UNAVAILABLE) {
            spot.blockForMaintenance();
        } else {
            spot.unblock();
        }

        Spot saved = spotPersistence.save(spot);
        publishSpotStatusChangedEventQuietly(saved);
        return saved;
    }

    private void publishSpotStatusChangedEventQuietly(Spot spot) {
        try {
            eventPublisher.publish(SpotStatusChangedEvent.of(spot, Instant.now()));
        } catch (RuntimeException e) {
            log.error("No se pudo publicar el evento de cambio de estado para la plaza {}", spot.getId(), e);
        }
    }
}
