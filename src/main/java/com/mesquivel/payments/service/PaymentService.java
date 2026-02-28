package com.mesquivel.payments.service;

import com.mesquivel.payments.dto.PaymentDTO.*;
import com.mesquivel.payments.exception.InvalidStatusTransitionException;
import com.mesquivel.payments.exception.PaymentNotFoundException;
import com.mesquivel.payments.model.Payment;
import com.mesquivel.payments.model.PaymentStatus;
import com.mesquivel.payments.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private static final Map<PaymentStatus, Set<PaymentStatus>> VALID_TRANSITIONS = Map.of(
            PaymentStatus.PENDING,    Set.of(PaymentStatus.PROCESSING, PaymentStatus.FAILED),
            PaymentStatus.PROCESSING, Set.of(PaymentStatus.COMPLETED, PaymentStatus.FAILED),
            PaymentStatus.COMPLETED,  Set.of(),
            PaymentStatus.FAILED,     Set.of()
    );

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher eventPublisher;
    private final PaymentMapper paymentMapper;

    public PaymentService(PaymentRepository paymentRepository,
                          PaymentEventPublisher eventPublisher,
                          PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
        this.paymentMapper = paymentMapper;
    }

    public PaymentResponse createPayment(CreatePaymentRequest request) {
        Payment payment = new Payment(
                request.concepto(),
                request.cantidadProductos(),
                request.pagador(),
                request.beneficiario(),
                request.montoTotal()
        );
        payment.setFechaCreacion(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);
        log.info("Pago creado | id={} | pagador={} | monto={}",
                saved.getId(), saved.getPagador(), saved.getMontoTotal());
        return paymentMapper.toResponse(saved);
    }

    public PaymentResponse getPaymentById(String id) {
        return paymentMapper.toResponse(findOrThrow(id));
    }

    public PaymentStatusResponse getPaymentStatus(String id) {
        return paymentMapper.toStatusResponse(findOrThrow(id));
    }

    public PaymentResponse updatePaymentStatus(String id, UpdateStatusRequest request) {
        Payment payment = findOrThrow(id);

        PaymentStatus estatusAnterior = payment.getEstatus(); 
        PaymentStatus estatusNuevo = request.estatus();

        validateTransition(estatusAnterior, estatusNuevo);

        payment.setEstatus(estatusNuevo);
        payment.setFechaActualizacion(LocalDateTime.now());
        Payment updated = paymentRepository.save(payment);

        try {
            eventPublisher.publishStatusChanged(updated, estatusAnterior);
        } 
        catch (Exception e) {
            log.error("Error publicando evento RabbitMQ | paymentId={} | error={}",id, e.getMessage());
            throw new RuntimeException("El estatus fue actualizado pero no se pudo notificar el evento. " + "Por favor reintente la notificación.", e);
        }

        log.info("Estatus actualizado | id={} | {} → {}", id, estatusAnterior, estatusNuevo);
        return paymentMapper.toResponse(updated);
    }

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    private Payment findOrThrow(String id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    private void validateTransition(PaymentStatus current, PaymentStatus next) {
        Set<PaymentStatus> allowed = VALID_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(next)) {
            throw new InvalidStatusTransitionException(current, next);
        }
    }
}
