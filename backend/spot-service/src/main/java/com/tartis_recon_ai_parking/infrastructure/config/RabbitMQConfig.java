package com.tartis_recon_ai_parking.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String STAY_CLOSED_QUEUE = "spot-service-stay-closed-queue";
    public static final String EXCHANGE = "parking-events-exchange";
    public static final String ROUTING_KEY_STAY_CLOSED = "stay-closed-v1";

    @Bean
    public DirectExchange parkingEventsExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue spotStayClosedQueue() {
        return new Queue(STAY_CLOSED_QUEUE);
    }

    @Bean
    public Binding bindingSpotStayClosed(Queue spotStayClosedQueue, DirectExchange parkingEventsExchange) {
        return BindingBuilder.bind(spotStayClosedQueue)
                .to(parkingEventsExchange)
                .with(ROUTING_KEY_STAY_CLOSED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
