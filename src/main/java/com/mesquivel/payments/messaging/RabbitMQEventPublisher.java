package com.mesquivel.payments.messaging;

import com.mesquivel.payments.config.RabbitMQConfig;
import com.mesquivel.payments.model.Payment;
import com.mesquivel.payments.model.PaymentStatus;
import com.mesquivel.payments.service.PaymentEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQEventPublisher implements PaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishStatusChanged(Payment payment, PaymentStatus estatusAnterior) {
        PaymentStatusChangedEvent event = PaymentStatusChangedEvent.of(
                payment.getId(),
                payment.getConcepto(),
                payment.getPagador(),
                payment.getBeneficiario(),
                payment.getMontoTotal(),
                estatusAnterior,        
                payment.getEstatus()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_PAYMENTS,
                RabbitMQConfig.ROUTING_KEY_STATUS,
                event
        );

        log.info("Evento publicado | paymentId={} | {} → {}",
                payment.getId(), estatusAnterior, payment.getEstatus());
    }
}