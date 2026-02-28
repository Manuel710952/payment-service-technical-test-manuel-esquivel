package com.mesquivel.payments.service;

import com.mesquivel.payments.dto.PaymentDTO.PaymentResponse;
import com.mesquivel.payments.dto.PaymentDTO.PaymentStatusResponse;
import com.mesquivel.payments.model.Payment;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;

@Component
public class PaymentMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getConcepto(),
                payment.getCantidadProductos(),
                payment.getPagador(),
                payment.getBeneficiario(),
                payment.getMontoTotal(),
                payment.getEstatus(),
                payment.getFechaCreacion() != null
                        ? payment.getFechaCreacion().format(FORMATTER) : null,
                payment.getFechaActualizacion() != null
                        ? payment.getFechaActualizacion().format(FORMATTER) : null
        );
    }

    public PaymentStatusResponse toStatusResponse(Payment payment) {
        return new PaymentStatusResponse(payment.getId(), payment.getEstatus());
    }
}
