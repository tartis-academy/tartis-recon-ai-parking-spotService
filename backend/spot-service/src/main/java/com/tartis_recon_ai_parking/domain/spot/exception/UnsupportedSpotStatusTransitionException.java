package com.tartis_recon_ai_parking.domain.spot.exception;

public class UnsupportedSpotStatusTransitionException extends SpotDomainException {

    public UnsupportedSpotStatusTransitionException(String message) {
        super(message);
    }
}
