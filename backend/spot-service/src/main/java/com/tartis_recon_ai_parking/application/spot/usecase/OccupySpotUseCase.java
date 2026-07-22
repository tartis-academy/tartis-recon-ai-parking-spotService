package com.tartis_recon_ai_parking.application.spot.usecase;

import com.tartis_recon_ai_parking.application.spot.port.output.SpotPersistence;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import com.tartis_recon_ai_parking.domain.spot.exception.NoAvailableSpotException;

public class OccupySpotUseCase {

    private final SpotPersistence spotPersistence;

    public OccupySpotUseCase(SpotPersistence spotPersistence) {
        this.spotPersistence = spotPersistence;
    }

    /**
     * RN-01: si no hay ninguna plaza disponible del tipo solicitado, se
     * deniega con NoAvailableSpotException (el controlador la traduce a
     * la respuesta HTTP correspondiente via CustomizedExceptionAdapter).
     */
    public Spot execute(VehicleType vehicleType) {
        return spotPersistence.findAndOccupyAvailableSpot(vehicleType)
                .orElseThrow(() -> new NoAvailableSpotException(
                        "No hay plazas disponibles para el tipo " + vehicleType));
    }
}