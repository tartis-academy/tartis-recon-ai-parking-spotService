package com.tartis_recon_ai_parking.infrastructure.spot.adapter.output.eventpublisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.tartis_recon_ai_parking.application.spot.dto.SpotStatusChangedEvent;
import com.tartis_recon_ai_parking.application.spot.port.output.SpotEventPublisher;
import com.tartis_recon_ai_parking.infrastructure.config.RabbitMQConfig;

@Component
public class SpotEventPublisherAdapter implements SpotEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public SpotEventPublisherAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(SpotStatusChangedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_SPOT_STATUS_CHANGED,
                event);
    }
}
