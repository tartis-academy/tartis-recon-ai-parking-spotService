package com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.eventlistener;

import com.tartis_recon_ai_parking.application.spot.usecase.ReleaseSpotUseCase;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotEventOutdatedException;
import com.tartis_recon_ai_parking.domain.spot.exception.SpotNotOccupiedException;
import com.tartis_recon_ai_parking.infrastructure.config.RabbitMQConfig;
import com.tartis_recon_ai_parking.infrastructure.spot.adapter.input.eventlistener.dto.StayClosedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SpotEventListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(SpotEventListenerAdapter.class);

    private final ReleaseSpotUseCase releaseSpotUseCase;

    public SpotEventListenerAdapter(ReleaseSpotUseCase releaseSpotUseCase) {
        this.releaseSpotUseCase = releaseSpotUseCase;
    }

    @RabbitListener(queues = RabbitMQConfig.STAY_CLOSED_QUEUE)
    public void handleStayClosedEvent(StayClosedEvent event) {
        log.info("Recibido StayClosedEvent: {}", event);

        try {
            Objects.requireNonNull(event, "Event cannot be null");
            Objects.requireNonNull(event.data(), "Event data cannot be null");
            Objects.requireNonNull(event.data().spotId(), "spotId cannot be null");

            releaseSpotUseCase.execute(event.data().spotId(), event.occurredAt());
            log.info("Plaza liberada exitosamente {}", event.data().spotId());

        } catch (SpotNotOccupiedException e) {
            // IDEMPOTENCIA: Si la plaza no está ocupada, significa que ya fue liberada
            // (ej. por un evento duplicado). Capturamos la excepción y la ignoramos 
            // para que RabbitMQ considere el mensaje procesado y no lo reintente infinitamente.
            log.warn("Idempotencia activada: La plaza {} ya estaba liberada o disponible. Ignorando evento duplicado.", event.data().spotId());
        } catch (SpotEventOutdatedException e) {
            // IDEMPOTENCIA AVANZADA: La plaza fue reasignada después de que se generara este evento.
            // Ignoramos el evento obsoleto para evitar liberar la plaza del nuevo inquilino.
            log.warn("Idempotencia activada: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error procesando StayClosedEvent: {}", e.getMessage(), e);
            // Relanzar la excepción para notificar a Spring AMQP.
            // NOTA: Con default-requeue-rejected=false y la DLQ configurada en la Queue (x-dead-letter-exchange),
            // RabbitMQ o RepublishMessageRecoverer enrutará el mensaje rechazado a la DLQ (spot-service-stay-closed-dlq) tras agotar los 6 reintentos.
            throw e;
        }
    }
}
