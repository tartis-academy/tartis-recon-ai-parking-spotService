package com.tartis_recon_ai_parking.domain.spot.exception;

public class SpotAlreadyOccupiedException extends SpotDomainException {

    public SpotAlreadyOccupiedException(String message) {
        super(message);
    }
}
