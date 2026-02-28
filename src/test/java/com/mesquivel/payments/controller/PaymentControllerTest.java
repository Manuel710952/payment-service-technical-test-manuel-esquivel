package com.mesquivel.payments.controller;

import com.mesquivel.payments.dto.PaymentDTO.*;
import com.mesquivel.payments.exception.InvalidStatusTransitionException;
import com.mesquivel.payments.exception.PaymentNotFoundException;
import com.mesquivel.payments.model.PaymentStatus;
import com.mesquivel.payments.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    private PaymentResponse buildResponse(PaymentStatus status) {
        return new PaymentResponse(
                "abc123", "Compra licencias", 5,
                "Juan", "Empresa ABC", new BigDecimal("1500.00"),
                status, "2024-01-01T10:00:00", null
        );
    }

    @Test
    @DisplayName("POST /payments to 201 Created con estatus PENDING")
    void createPayment_shouldReturn201() throws Exception {
        when(paymentService.createPayment(any()))
                .thenReturn(buildResponse(PaymentStatus.PENDING));

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreatePaymentRequest("Compra licencias", 5,
                                        "Juan", "Empresa ABC", new BigDecimal("1500.00")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estatus").value("PENDING"))
                .andExpect(jsonPath("$.id").value("abc123"));
    }

    @Test
    @DisplayName("POST /payments sin concepto to 400 Bad Request")
    void createPayment_missingField_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreatePaymentRequest("", 5,
                                        "Juan", "Empresa ABC", new BigDecimal("1500.00")))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /payments/{id}/status to 200 con estatus actual")
    void getStatus_shouldReturn200() throws Exception {
        when(paymentService.getPaymentStatus("abc123"))
                .thenReturn(new PaymentStatusResponse("abc123", PaymentStatus.PENDING));

        mockMvc.perform(get("/api/v1/payments/abc123/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estatus").value("PENDING"));
    }

    @Test
    @DisplayName("GET /payments/{id}/status to 404 si no existe")
    void getStatus_notFound_shouldReturn404() throws Exception {
        when(paymentService.getPaymentStatus("nope"))
                .thenThrow(new PaymentNotFoundException("nope"));

        mockMvc.perform(get("/api/v1/payments/nope/status"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("PATCH /payments/{id}/status to 200 con nuevo estatus")
    void updateStatus_shouldReturn200() throws Exception {
        when(paymentService.updatePaymentStatus(eq("abc123"), any()))
        .thenReturn(buildResponse(PaymentStatus.PROCESSING));

	mockMvc.perform(patch("/api/v1/payments/abc123/status")
	                .contentType(MediaType.APPLICATION_JSON)
	                .content(objectMapper.writeValueAsString(
	                        new UpdateStatusRequest(PaymentStatus.PROCESSING))))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.estatus").value("PROCESSING"));
	}
	
	@Test
	@DisplayName("PATCH /payments/{id}/status to 422 transición inválida")
	void updateStatus_invalidTransition_shouldReturn422() throws Exception {
	when(paymentService.updatePaymentStatus(eq("abc123"), any()))
	        .thenThrow(new InvalidStatusTransitionException(
	                PaymentStatus.COMPLETED, PaymentStatus.PENDING));
	
	mockMvc.perform(patch("/api/v1/payments/abc123/status")
	                .contentType(MediaType.APPLICATION_JSON)
	                .content(objectMapper.writeValueAsString(
	                        new UpdateStatusRequest(PaymentStatus.PENDING))))
	        .andExpect(status().isUnprocessableEntity())
	        .andExpect(jsonPath("$.message").exists());
	}
}
