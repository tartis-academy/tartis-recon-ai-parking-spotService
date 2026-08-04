package com.tartis_recon_ai_parking.application.spot.usecase;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.tartis_recon_ai_parking.application.spot.dto.SpotStatusChangedEvent;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotEventPublisher;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import com.tartis_recon_ai_parking.domain.spot.exception.NoAvailableSpotException;

public class OccupySpotUseCase {

    private static final Logger log = LoggerFactory.getLogger(OccupySpotUseCase.class);

    private final SpotPersistence spotPersistence;
    private final SpotEventPublisher eventPublisher;

    public OccupySpotUseCase(SpotPersistence spotPersistence, SpotEventPublisher eventPublisher) {
        this.spotPersistence = spotPersistence;
        this.eventPublisher = eventPublisher;
    }

    /**
     * RN-01: si no hay ninguna plaza disponible del tipo solicitado, se
     * deniega con NoAvailableSpotException (el controlador la traduce a
     * la respuesta HTTP correspondiente via CustomizedExceptionAdapter).
     */
    @Transactional
    public Spot execute(VehicleType vehicleType) {
        Spot occupied = spotPersistence.findAndOccupyAvailableSpot(vehicleType)
                .orElseThrow(() -> new NoAvailableSpotException(
                        "No hay plazas disponibles para el tipo " + vehicleType));
        publishSpotStatusChangedEventQuietly(occupied);
        return occupied;
    }

    private void publishSpotStatusChangedEventQuietly(Spot spot) {
        try {
            eventPublisher.publish(SpotStatusChangedEvent.of(spot, Instant.now()));
        } catch (RuntimeException e) {
            log.error("No se pudo publicar el evento de cambio de estado para la plaza {}", spot.getId(), e);
        }
    }
}
