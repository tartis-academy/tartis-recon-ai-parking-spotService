package com.tartis_recon_ai_parking.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
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
    public TopicExchange parkingEventsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue spotStayClosedQueue() {
        return org.springframework.amqp.core.QueueBuilder.durable(STAY_CLOSED_QUEUE)
                .withArgument("x-dead-letter-exchange", "spot-service-stay-closed-dlx")
                .withArgument("x-dead-letter-routing-key", "spot-service-stay-closed-dead-letter")
                .build();
    }

    @Bean
    public Binding bindingSpotStayClosed(Queue spotStayClosedQueue, TopicExchange parkingEventsExchange) {
        return BindingBuilder.bind(spotStayClosedQueue)
                .to(parkingEventsExchange)
                .with(ROUTING_KEY_STAY_CLOSED);
    }

    // =========================================================================
    // DEAD LETTER QUEUE (DLQ) & EXCHANGE (DLX) PARA CONSUMIDOR SPOT SERVICE
    // =========================================================================
    @Bean
    public TopicExchange spotStayClosedDLX() {
        return new TopicExchange("spot-service-stay-closed-dlx");
    }

    @Bean
    public Queue spotStayClosedDLQ() {
        return org.springframework.amqp.core.QueueBuilder.durable("spot-service-stay-closed-dlq").build();
    }

    @Bean
    public Binding bindingSpotStayClosedDLQ(Queue spotStayClosedDLQ, TopicExchange spotStayClosedDLX) {
        return BindingBuilder.bind(spotStayClosedDLQ)
                .to(spotStayClosedDLX)
                .with("spot-service-stay-closed-dead-letter");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
