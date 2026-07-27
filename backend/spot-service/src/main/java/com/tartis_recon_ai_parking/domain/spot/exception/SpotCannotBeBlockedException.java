package com.tartis_recon_ai_parking.domain.spot.exception;

public class SpotCannotBeBlockedException extends RuntimeException {

    public SpotCannotBeBlockedException(String message) {
        super(message);
    }
}
