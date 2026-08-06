package com.tartis_recon_ai_parking.infrastructure.spot.adapter.output.eventpublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import com.tartis_recon_ai_parking.application.spot.dto.SpotStatusChangedEvent;
import com.tartis_recon_ai_parking.domain.spot.Spot;
import com.tartis_recon_ai_parking.domain.spot.SpotStatus;
import com.tartis_recon_ai_parking.domain.spot.VehicleType;
import com.tartis_recon_ai_parking.infrastructure.config.RabbitMQConfig;

/**
 * Los tests de los use cases mockean SpotEventPublisher directamente y no
 * detectan un exchange o routing key mal configurados en el adaptador real;
 * este test ejercita SpotEventPublisherAdapter y su serializacion.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SpotEventPublisherAdapter Tests")
class SpotEventPublisherAdapterTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private SpotEventPublisherAdapter adapter;

    @Test
    @DisplayName("Debe publicar en parking-events-exchange con la routing key spot-status-changed-v1")
    void publish_ShouldSendToParkingEventsExchangeWithSpotStatusChangedRoutingKey() {
        SpotStatusChangedEvent event = SpotStatusChangedEvent.of(
                Spot.reconstruct(UUID.randomUUID(), VehicleType.CAR, SpotStatus.OCCUPIED), Instant.now());

        adapter.publish(event);

        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_SPOT_STATUS_CHANGED,
                event);
    }

    @Test
    @DisplayName("El evento debe serializarse a JSON con los campos esperados")
    void publish_EventShouldSerializeToExpectedJson() {
        UUID spotId = UUID.randomUUID();
        SpotStatusChangedEvent event = SpotStatusChangedEvent.of(
                Spot.reconstruct(spotId, VehicleType.CAR, SpotStatus.OCCUPIED),
                Instant.parse("2026-08-05T10:00:00Z"));

        Message message = new Jackson2JsonMessageConverter().toMessage(event, new MessageProperties());
        String json = new String(message.getBody(), StandardCharsets.UTF_8);

        assertThat(message.getMessageProperties().getContentType()).isEqualTo("application/json");
        assertThat(json).contains("\"type\":\"SpotStatusChangedEvent\"");
        assertThat(json).contains("\"version\":\"v1\"");
        assertThat(json).contains("\"spotId\":\"" + spotId + "\"");
        assertThat(json).contains("\"status\":\"OCCUPIED\"");
        assertThat(json).contains("\"vehicleType\":\"CAR\"");
    }
}
