package com.tartis_recon_ai_parking.application.spot.usecase;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import com.tartis_recon_ai_parking.application.spot.dto.SpotStatusChangedEvent;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import com.tartis_recon_ai_parking.domain.spot.exception.NoAvailableSpotException;

public class OccupySpotUseCase {

    private final SpotPersistence spotPersistence;
    private final ApplicationEventPublisher applicationEventPublisher;

    public OccupySpotUseCase(SpotPersistence spotPersistence, ApplicationEventPublisher applicationEventPublisher) {
        this.spotPersistence = spotPersistence;
        this.applicationEventPublisher = applicationEventPublisher;
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
        applicationEventPublisher.publishEvent(SpotStatusChangedEvent.of(occupied, occupied.getLastStatusChangeAt()));
        return occupied;
    }
}
