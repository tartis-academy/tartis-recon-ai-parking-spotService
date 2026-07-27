package com.tartis_recon_ai_parking.domain.spot.exception;

/**
 * RN-01: se lanza cuando no hay ninguna plaza AVAILABLE del tipo solicitado.
 * El sistema debe denegar el acceso al vehiculo (barrera cerrada) en este caso.
 */
public class NoAvailableSpotException extends SpotDomainException {

    public NoAvailableSpotException(String message) {
        super(message);
    }
}