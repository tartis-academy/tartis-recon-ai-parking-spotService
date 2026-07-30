package com.tartis_recon_ai_parking.domain.spot.exception;

public class SpotTypeChangeNotAllowedException extends SpotDomainException {

    public SpotTypeChangeNotAllowedException(String message) {
        super(message);
    }
}
