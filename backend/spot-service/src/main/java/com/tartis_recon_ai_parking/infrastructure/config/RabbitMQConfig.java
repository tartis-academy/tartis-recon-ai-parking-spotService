package com.tartis_recon_ai_parking.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String STAY_CLOSED_QUEUE = "spot-service-stay-closed-queue";
    public static final String EXCHANGE = "parking-events-exchange";
    public static final String ROUTING_KEY_STAY_CLOSED = "stay-closed-v1";
    public static final String DLX_EXCHANGE = "spot-service-stay-closed-dlx";
    public static final String DLQ_ROUTING_KEY = "spot-service-stay-closed-dead-letter";
    public static final String DLQ_NAME = "spot-service-stay-closed-dlq";

    @Bean
    public TopicExchange parkingEventsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue spotStayClosedQueue() {
        return QueueBuilder.durable(STAY_CLOSED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
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
        return new TopicExchange(DLX_EXCHANGE);
    }

    @Bean
    public Queue spotStayClosedDLQ() {
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    @Bean
    public Binding bindingSpotStayClosedDLQ(Queue spotStayClosedDLQ, TopicExchange spotStayClosedDLX) {
        return BindingBuilder.bind(spotStayClosedDLQ)
                .to(spotStayClosedDLX)
                .with(DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(rabbitTemplate, DLX_EXCHANGE, DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
