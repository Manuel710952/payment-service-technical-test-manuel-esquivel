package com.mesquivel.payments.messaging;

import com.mesquivel.payments.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer 2: Notificaciones
 * Envía mensajes al pagador según el nuevo estatus.
 * Si falla después de los reintentos, el mensaje va a la DLQ
 * para no perder la notificación pendiente.
 */
@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    /**
     * Nota: aqui es donde se podria integrar con servicio de email/SMS/push o cualquier otro medio de notificacion de este tipo,
     * de momento solo registarar la notificacion con fines del ejercicio practico
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICATION)
    public void handleStatusChanged(PaymentStatusChangedEvent event) {
        String mensaje = buildMensaje(event);
        log.info("[NOTIFICATION] Para={} | mensaje={}",
                event.pagador(), mensaje);
    }

    private String buildMensaje(PaymentStatusChangedEvent event) {
        return switch (event.estatusNuevo()) {
            case PROCESSING ->
                String.format("Su pago '%s' por $%s está siendo procesado.",
                        event.concepto(), event.montoTotal());
            case COMPLETED ->
                String.format("Su pago '%s' por $%s fue completado exitosamente.",
                        event.concepto(), event.montoTotal());
            case FAILED ->
                String.format("Su pago '%s' por $%s ha fallado. Contacte a soporte.",
                        event.concepto(), event.montoTotal());
            default ->
                String.format("El estatus de su pago '%s' cambió a %s.",
                        event.concepto(), event.estatusNuevo());
        };
    }
}
