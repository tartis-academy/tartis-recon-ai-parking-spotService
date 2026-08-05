package com.tartis_recon_ai_parking.infrastructure.spot.adapter.output.eventpublisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tartis_recon_ai_parking.application.spot.dto.SpotStatusChangedEvent;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotEventPublisher;

/**
 * Los use cases publican SpotStatusChangedEvent como evento de aplicacion
 * (in-process) dentro de su @Transactional; este listener lo reenvia al
 * SpotEventPublisher real solo si la transaccion hace commit, evitando que
 * RabbitMQ reciba un evento de un cambio que finalmente no quedo persistido.
 */
@Component
public class SpotStatusChangedEventRelay {

    private static final Logger log = LoggerFactory.getLogger(SpotStatusChangedEventRelay.class);

    private final SpotEventPublisher eventPublisher;

    public SpotStatusChangedEventRelay(SpotEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSpotStatusChanged(SpotStatusChangedEvent event) {
        try {
            eventPublisher.publish(event);
        } catch (RuntimeException e) {
            log.error("No se pudo publicar el evento de cambio de estado para la plaza {}", event.data().spotId(), e);
        }
    }
}
