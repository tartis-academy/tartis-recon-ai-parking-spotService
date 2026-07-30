package com.tartis_recon_ai_parking.domain.spot.exception;

public class SpotEventOutdatedException extends RuntimeException {
    public SpotEventOutdatedException(String message) {
        super(message);
    }
}
