package com.mesquivel.payments.exception;

import com.mesquivel.payments.model.PaymentStatus;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(PaymentStatus current, PaymentStatus next) {
        super("Transición de estatus inválida: " + current + " to " + next);
    }
}
