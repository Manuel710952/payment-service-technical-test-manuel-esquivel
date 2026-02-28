package com.mesquivel.payments.messaging;

import com.mesquivel.payments.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento publicado en RabbitMQ cuando el estatus de un pago cambia.
 * Incluye estatusAnterior para que los consumers tengan contexto completo
 * del cambio sin necesidad de consultar la base de datos.
 */
public record PaymentStatusChangedEvent(
        String paymentId,
        String concepto,
        String pagador,
        String beneficiario,
        BigDecimal montoTotal,
        PaymentStatus estatusAnterior,
        PaymentStatus estatusNuevo,
        LocalDateTime fechaCambio
) {
    public static PaymentStatusChangedEvent of(
            String paymentId,
            String concepto,
            String pagador,
            String beneficiario,
            BigDecimal montoTotal,
            PaymentStatus estatusAnterior,
            PaymentStatus estatusNuevo
    ) {
        return new PaymentStatusChangedEvent(
                paymentId, concepto, pagador, beneficiario,
                montoTotal, estatusAnterior, estatusNuevo,
                LocalDateTime.now()
        );
    }
}
