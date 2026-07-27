package com.tartis_recon_ai_parking.domain.spot.exception;

public class SpotNotOccupiedException extends RuntimeException {

    public SpotNotOccupiedException(String message) {
        super(message);
    }
}
