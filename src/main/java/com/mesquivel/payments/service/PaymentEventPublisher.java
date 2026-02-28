package com.mesquivel.payments.service;

import com.mesquivel.payments.model.Payment;
import com.mesquivel.payments.model.PaymentStatus;

public interface PaymentEventPublisher {
    void publishStatusChanged(Payment payment, PaymentStatus estatusAnterior);
}
