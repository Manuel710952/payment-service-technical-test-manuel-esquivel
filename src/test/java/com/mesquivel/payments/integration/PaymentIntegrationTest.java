package com.mesquivel.payments.integration;

import com.mesquivel.payments.dto.PaymentDTO.*;
import com.mesquivel.payments.model.PaymentStatus;
import com.mesquivel.payments.repository.PaymentRepository;
import com.mesquivel.payments.service.PaymentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
class PaymentIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer =
            new MongoDBContainer("mongo:6.0");

    @Container
    static RabbitMQContainer rabbitMQContainer =
            new RabbitMQContainer("rabbitmq:3.12-management");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
    }

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @AfterEach
    void cleanUp() {
        paymentRepository.deleteAll();
    }

    @Test
    @DisplayName("Flujo completo: crear pago y cambiar estatus a PROCESSING")
    void fullFlow_createAndUpdateStatus() {
        CreatePaymentRequest request = new CreatePaymentRequest(
                "Compra de licencias", 5,
                "Juan Pérez", "Empresa ABC",
                new BigDecimal("1500.00")
        );
        PaymentResponse created = paymentService.createPayment(request);

        assertThat(created.id()).isNotNull();
        assertThat(created.estatus()).isEqualTo(PaymentStatus.PENDING);

        // Cambiar estatus
        PaymentResponse updated = paymentService.updatePaymentStatus(
                created.id(),
                new UpdateStatusRequest(PaymentStatus.PROCESSING)
        );

        assertThat(updated.estatus()).isEqualTo(PaymentStatus.PROCESSING);
        assertThat(updated.fechaActualizacion()).isNotNull();
    }

    @Test
    @DisplayName("Flujo completo: PENDING to PROCESSING to COMPLETED")
    void fullFlow_pendingToCompleted() {
        CreatePaymentRequest request = new CreatePaymentRequest(
                "Pago de servicios", 2,
                "María López", "Proveedor XYZ",
                new BigDecimal("500.00")
        );
        PaymentResponse created = paymentService.createPayment(request);

        paymentService.updatePaymentStatus(created.id(),
                new UpdateStatusRequest(PaymentStatus.PROCESSING));

        PaymentResponse completed = paymentService.updatePaymentStatus(created.id(),
                new UpdateStatusRequest(PaymentStatus.COMPLETED));

        assertThat(completed.estatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    @DisplayName("Verificar estatus directamente desde base de datos")
    void verifyStatusInDatabase() {
        CreatePaymentRequest request = new CreatePaymentRequest(
                "Pago directo", 1,
                "Carlos", "Empresa Z",
                new BigDecimal("100.00")
        );
        PaymentResponse created = paymentService.createPayment(request);

        var savedPayment = paymentRepository.findById(created.id());

        assertThat(savedPayment).isPresent();
        assertThat(savedPayment.get().getEstatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(savedPayment.get().getPagador()).isEqualTo("Carlos");
    }
}
