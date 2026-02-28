package com.mesquivel.payments.service;

import com.mesquivel.payments.dto.PaymentDTO.*;
import com.mesquivel.payments.exception.InvalidStatusTransitionException;
import com.mesquivel.payments.exception.PaymentNotFoundException;
import com.mesquivel.payments.model.Payment;
import com.mesquivel.payments.model.PaymentStatus;
import com.mesquivel.payments.repository.PaymentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventPublisher eventPublisher;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentService paymentService;

    private Payment samplePayment;

    @BeforeEach
    void setUp() {
        samplePayment = new Payment(
                "Compra de licencias",
                5,
                "Juan Pérez",
                "Empresa ABC",
                new BigDecimal("1500.00")
        );
        samplePayment.setId("abc123");
        samplePayment.setFechaCreacion(LocalDateTime.now());
    }

    @Test
    @DisplayName("createPayment: debe crear un pago con estatus PENDING")
    void createPayment_shouldReturnPendingStatus() {
        when(paymentRepository.save(any(Payment.class))).thenReturn(samplePayment);
        when(paymentMapper.toResponse(any())).thenReturn(
                new PaymentResponse("abc123", "Compra de licencias", 5,
                        "Juan Pérez", "Empresa ABC", new BigDecimal("1500.00"),
                        PaymentStatus.PENDING, null, null));

        CreatePaymentRequest request = new CreatePaymentRequest(
                "Compra de licencias", 5, "Juan Pérez",
                "Empresa ABC", new BigDecimal("1500.00")
        );

        PaymentResponse response = paymentService.createPayment(request);

        assertThat(response.estatus()).isEqualTo(PaymentStatus.PENDING);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("getPaymentStatus: debe lanzar excepción si el pago no existe")
    void getPaymentStatus_shouldThrowIfNotFound() {
        when(paymentRepository.findById("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentStatus("nope"))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("nope");
    }

    @Test
    @DisplayName("updateStatus: transición válida PENDING to PROCESSING publica evento")
    void updateStatus_validTransition_publishesEvent() {
        when(paymentRepository.findById("abc123")).thenReturn(Optional.of(samplePayment));
        when(paymentRepository.save(any())).thenReturn(samplePayment);
        when(paymentMapper.toResponse(any())).thenReturn(
                new PaymentResponse("abc123", "Compra de licencias", 5,
                        "Juan Pérez", "Empresa ABC", new BigDecimal("1500.00"),
                        PaymentStatus.PROCESSING, null, null));

        paymentService.updatePaymentStatus("abc123",
                new UpdateStatusRequest(PaymentStatus.PROCESSING));

        verify(eventPublisher, times(1))
                .publishStatusChanged(any(Payment.class), eq(PaymentStatus.PENDING));
    }

    @Test
    @DisplayName("updateStatus: transición inválida PENDING to COMPLETED lanza excepción")
    void updateStatus_invalidTransition_shouldThrow() {
        when(paymentRepository.findById("abc123")).thenReturn(Optional.of(samplePayment));
        assertThatThrownBy(() -> paymentService.updatePaymentStatus("abc123",
                new UpdateStatusRequest(PaymentStatus.COMPLETED)))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("PENDING")
                .hasMessageContaining("COMPLETED");

        verify(eventPublisher, never()).publishStatusChanged(any(), any());
    }

    @Test
    @DisplayName("updateStatus: COMPLETED es estado terminal, no permite cambios")
    void updateStatus_completedIsTerminal_shouldThrow() {
        samplePayment.setEstatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findById("abc123")).thenReturn(Optional.of(samplePayment));

        assertThatThrownBy(() -> paymentService.updatePaymentStatus("abc123",
                new UpdateStatusRequest(PaymentStatus.FAILED)))
                .isInstanceOf(InvalidStatusTransitionException.class);

        verify(eventPublisher, never()).publishStatusChanged(any(), any());
    }

    @Test
    @DisplayName("updateStatus: PROCESSING to COMPLETED es válido")
    void updateStatus_processingToCompleted_shouldSucceed() {
        samplePayment.setEstatus(PaymentStatus.PROCESSING);
        when(paymentRepository.findById("abc123")).thenReturn(Optional.of(samplePayment));
        when(paymentRepository.save(any())).thenReturn(samplePayment);
        when(paymentMapper.toResponse(any())).thenReturn(
                new PaymentResponse("abc123", "Compra de licencias", 5,
                        "Juan Pérez", "Empresa ABC", new BigDecimal("1500.00"),
                        PaymentStatus.COMPLETED, null, null));

        paymentService.updatePaymentStatus("abc123",
                new UpdateStatusRequest(PaymentStatus.COMPLETED));

        verify(eventPublisher, times(1))
                .publishStatusChanged(any(), eq(PaymentStatus.PROCESSING));
    }
}

