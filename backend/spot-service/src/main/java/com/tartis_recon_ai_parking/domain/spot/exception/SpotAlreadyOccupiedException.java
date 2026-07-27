package com.tartis_recon_ai_parking.domain.spot.exception;

public class SpotAlreadyOccupiedException extends RuntimeException {

    public SpotAlreadyOccupiedException(String message) {
        super(message);
    }
}
