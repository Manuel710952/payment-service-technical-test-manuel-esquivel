package com.mesquivel.payments.exception;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String id) {
        super("Pago no encontrado con id: " + id);
    }
}
