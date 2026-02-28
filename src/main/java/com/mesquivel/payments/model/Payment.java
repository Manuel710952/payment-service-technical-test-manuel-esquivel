package com.mesquivel.payments.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    @NotBlank(message = "El concepto es obligatorio")
    private String concepto;

    @NotNull(message = "La cantidad de productos es obligatoria")
    @Positive(message = "La cantidad de productos debe ser mayor a 0")
    private Integer cantidadProductos;

    @NotBlank(message = "El pagador es obligatorio")
    private String pagador;

    @NotBlank(message = "El beneficiario es obligatorio")
    private String beneficiario;

    @NotNull(message = "El monto total es obligatorio")
    @Positive(message = "El monto total debe ser mayor a 0")
    private BigDecimal montoTotal;

    private PaymentStatus estatus;

    @CreatedDate
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    private LocalDateTime fechaActualizacion;

    public Payment() {}

    public Payment(String concepto, Integer cantidadProductos, String pagador,
                   String beneficiario, BigDecimal montoTotal) {
        this.concepto = concepto;
        this.cantidadProductos = cantidadProductos;
        this.pagador = pagador;
        this.beneficiario = beneficiario;
        this.montoTotal = montoTotal;
        this.estatus = PaymentStatus.PENDING;
    }

    public String getId() { 
    	return id; 
    }
    
    public void setId(String id) { 
    	this.id = id; 
    }

    public String getConcepto() { 
    	return concepto; 
    }
    
    public void setConcepto(String concepto) { 
    	this.concepto = concepto; 
    }

    public Integer getCantidadProductos() { 
    	return cantidadProductos; 
    }
    public void setCantidadProductos(Integer cantidadProductos) { 
    	this.cantidadProductos = cantidadProductos; 
    }

    public String getPagador() { 
    	return pagador; 
    }
    public void setPagador(String pagador) { 
    	this.pagador = pagador; 
    }

    public String getBeneficiario() { 
    	return beneficiario; 
    }
    public void setBeneficiario(String beneficiario) { 
    	this.beneficiario = beneficiario; 
    }

    public BigDecimal getMontoTotal() { 
    	return montoTotal; 
    }
    
    public void setMontoTotal(BigDecimal montoTotal) { 
    	this.montoTotal = montoTotal; 
    }

    public PaymentStatus getEstatus() { 
    	return estatus; 
    }
    
    public void setEstatus(PaymentStatus estatus) { 
    	this.estatus = estatus; 
    }

    public LocalDateTime getFechaCreacion() { 
    	return fechaCreacion; 
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) { 
    	this.fechaCreacion = fechaCreacion; 
    }

    public LocalDateTime getFechaActualizacion() { 
    	return fechaActualizacion; 
    }
    
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { 
    	this.fechaActualizacion = fechaActualizacion; 
    }
}
