package com.tartis_recon_ai_parking.application.spot.port.output;

import com.tartis_recon_ai_parking.application.spot.dto.SpotStatusChangedEvent;

public interface SpotEventPublisher {

    /**
     * Publica el evento SpotStatusChangedEvent de forma asincrona hacia RabbitMQ.
     */
    void publish(SpotStatusChangedEvent event);
}
