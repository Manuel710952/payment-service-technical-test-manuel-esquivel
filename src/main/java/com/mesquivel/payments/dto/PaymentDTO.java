package com.mesquivel.payments.dto;

import com.mesquivel.payments.model.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class PaymentDTO {

    public record CreatePaymentRequest(
            @NotBlank(message = "El concepto es obligatorio")
            String concepto,

            @NotNull(message = "La cantidad de productos es obligatoria")
            @Positive(message = "La cantidad de productos debe ser mayor a 0")
            Integer cantidadProductos,

            @NotBlank(message = "El pagador es obligatorio")
            String pagador,

            @NotBlank(message = "El beneficiario es obligatorio")
            String beneficiario,

            @NotNull(message = "El monto total es obligatorio")
            @Positive(message = "El monto total debe ser mayor a 0")
            BigDecimal montoTotal
    ) {}

    public record UpdateStatusRequest(
            @NotNull(message = "El estatus es obligatorio")
            PaymentStatus estatus
    ) {}

    public record PaymentResponse(
            String id,
            String concepto,
            Integer cantidadProductos,
            String pagador,
            String beneficiario,
            BigDecimal montoTotal,
            PaymentStatus estatus,
            String fechaCreacion,
            String fechaActualizacion
    ) {}

    public record PaymentStatusResponse(
            String id,
            PaymentStatus estatus
    ) {}
}
