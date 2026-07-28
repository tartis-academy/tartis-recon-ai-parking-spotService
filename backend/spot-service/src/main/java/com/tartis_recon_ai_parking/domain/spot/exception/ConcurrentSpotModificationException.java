package com.tartis_recon_ai_parking.domain.spot.exception;
 
/**
 * Se lanza cuando dos operaciones concurrentes intentan modificar la misma
 * plaza y una de ellas trabaja con datos obsoletos (deteccion via bloqueo
 * optimista - campo @Version en SpotEntity).
 *
 * Ejemplo real: un administrador bloquea una plaza para mantenimiento
 * (PATCH /{id}/status) justo cuando el sistema la acaba de ocupar
 * (POST /occupy). Sin este control, el UPDATE del administrador
 * sobreescribiria en silencio el estado OCCUPIED, violando IN-05/IN-25.
 *
 * El controlador debe traducir esto a HTTP 409 (Conflict).
 */
public class ConcurrentSpotModificationException extends RuntimeException {
 
    public ConcurrentSpotModificationException(String message) {
        super(message);
    }
}