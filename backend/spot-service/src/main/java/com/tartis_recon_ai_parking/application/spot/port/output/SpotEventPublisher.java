package com.tartis_recon_ai_parking.application.spot.port.output;

import com.tartis_recon_ai_parking.application.spot.dto.SpotStatusChangedEvent;

public interface SpotEventPublisher {

    /**
     * Publica el evento SpotStatusChangedEvent hacia RabbitMQ. La llamada es
     * sincrona: el invocador (SpotStatusChangedEventRelay, en AFTER_COMMIT)
     * espera a que RabbitTemplate.convertAndSend retorne.
     */
    void publish(SpotStatusChangedEvent event);
}
