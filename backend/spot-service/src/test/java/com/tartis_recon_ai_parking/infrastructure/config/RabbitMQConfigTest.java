package com.tartis_recon_ai_parking.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RabbitMQConfigTest {

    private final RabbitMQConfig config = new RabbitMQConfig();

    @Test
    void debeConfigurarExchangeYColaConArgumentosDLQ() {
        TopicExchange exchange = config.parkingEventsExchange();
        assertEquals(RabbitMQConfig.EXCHANGE, exchange.getName());
        assertEquals("topic", exchange.getType());

        Queue queue = config.spotStayClosedQueue();
        assertEquals(RabbitMQConfig.STAY_CLOSED_QUEUE, queue.getName());
        assertEquals("spot-service-stay-closed-dlx", queue.getArguments().get("x-dead-letter-exchange"));
        assertEquals("spot-service-stay-closed-dead-letter", queue.getArguments().get("x-dead-letter-routing-key"));
    }

    @Test
    void debeConfigurarBindingDeColaPrincipal() {
        Queue queue = config.spotStayClosedQueue();
        TopicExchange exchange = config.parkingEventsExchange();

        Binding binding = config.bindingSpotStayClosed(queue, exchange);
        assertEquals(RabbitMQConfig.STAY_CLOSED_QUEUE, binding.getDestination());
        assertEquals(RabbitMQConfig.EXCHANGE, binding.getExchange());
        assertEquals(RabbitMQConfig.ROUTING_KEY_STAY_CLOSED, binding.getRoutingKey());
    }

    @Test
    void debeConfigurarInfraestructuraDLQ() {
        TopicExchange dlx = config.spotStayClosedDLX();
        assertEquals("spot-service-stay-closed-dlx", dlx.getName());

        Queue dlq = config.spotStayClosedDLQ();
        assertEquals("spot-service-stay-closed-dlq", dlq.getName());

        Binding binding = config.bindingSpotStayClosedDLQ(dlq, dlx);
        assertEquals("spot-service-stay-closed-dlq", binding.getDestination());
        assertEquals("spot-service-stay-closed-dlx", binding.getExchange());
        assertEquals("spot-service-stay-closed-dead-letter", binding.getRoutingKey());
    }

    @Test
    void debeRegistrarJsonMessageConverterYMessageRecoverer() {
        assertNotNull(config.jsonMessageConverter());
        org.springframework.amqp.rabbit.core.RabbitTemplate mockTemplate = org.mockito.Mockito.mock(org.springframework.amqp.rabbit.core.RabbitTemplate.class);
        assertNotNull(config.messageRecoverer(mockTemplate));
    }
}
