package com.mesquivel.payments.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_PAYMENTS     = "payments.exchange";
    public static final String EXCHANGE_DLQ          = "payments.dlq.exchange";
    public static final String QUEUE_AUDIT           = "payments.audit.queue";
    public static final String QUEUE_NOTIFICATION    = "payments.notification.queue";
    public static final String QUEUE_AUDIT_DLQ       = "payments.audit.dlq";
    public static final String QUEUE_NOTIFICATION_DLQ= "payments.notification.dlq";
    public static final String ROUTING_KEY_STATUS    = "payment.status.changed";
    public static final String ROUTING_KEY_DEAD      = "payment.dead";

    @Bean
    public TopicExchange paymentsExchange() {
        return new TopicExchange(EXCHANGE_PAYMENTS, true, false);
    }

    @Bean
    public DirectExchange dlqExchange() {
        return new DirectExchange(EXCHANGE_DLQ, true, false);
    }

    @Bean
    public Queue auditQueue() {
        return QueueBuilder.durable(QUEUE_AUDIT)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLQ)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_DEAD)
                .build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(QUEUE_NOTIFICATION)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLQ)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_DEAD)
                .build();
    }

    @Bean
    public Queue auditDlq() {
        return QueueBuilder.durable(QUEUE_AUDIT_DLQ).build();
    }

    @Bean
    public Queue notificationDlq() {
        return QueueBuilder.durable(QUEUE_NOTIFICATION_DLQ).build();
    }

    @Bean
    public Binding auditBinding() {
        return BindingBuilder.bind(auditQueue())
                .to(paymentsExchange())
                .with(ROUTING_KEY_STATUS);
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(paymentsExchange())
                .with(ROUTING_KEY_STATUS);
    }

    @Bean
    public Binding auditDlqBinding() {
        return BindingBuilder.bind(auditDlq())
                .to(dlqExchange())
                .with(ROUTING_KEY_DEAD);
    }

    @Bean
    public Binding notificationDlqBinding() {
        return BindingBuilder.bind(notificationDlq())
                .to(dlqExchange())
                .with(ROUTING_KEY_DEAD);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}