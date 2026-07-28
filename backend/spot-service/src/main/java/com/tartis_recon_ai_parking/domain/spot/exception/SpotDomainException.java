package com.tartis_recon_ai_parking.domain.spot.exception;

public abstract class SpotDomainException extends RuntimeException {

    protected SpotDomainException(String message) {
        super(message);
    }
}
