package com.tartis_recon_ai_parking.domain.spot.exception;

public class SpotNotFoundException extends RuntimeException{

    public SpotNotFoundException(String message) {
        super(message);
    }
}
