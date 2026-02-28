package com.mesquivel.payments.messaging;

import com.mesquivel.payments.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer 1: Auditoría
 * Registra cada cambio de estatus para trazabilidad completa.
 * Si falla después de los reintentos, el mensaje va a la DLQ
 * para revisión manual sin pérdida de información.
 */
@Component
public class AuditConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_AUDIT)
    public void handleStatusChanged(PaymentStatusChangedEvent event) {
        log.info("[AUDIT] paymentId={} | pagador={} | beneficiario={} | " +
                 "monto={} | {} → {} | fecha={}",
                event.paymentId(),
                event.pagador(),
                event.beneficiario(),
                event.montoTotal(),
                event.estatusAnterior(),
                event.estatusNuevo(),
                event.fechaCambio()
        );
    }
}
