# Documentación RabbitMQ — Payment Service

## Exchange principal

| Propiedad   | Valor               |
|-------------|---------------------|
| Nombre      | payments.exchange |
| Tipo        | Topic             |
| Durable     | true              |
| Auto-delete | false             |

Se usa Topic Exchange porque permite que múltiples queues reciban
el mismo evento. En el futuro se pueden agregar más consumers
sin modificar el publisher.

## Exchange Dead Letter

| Propiedad   | Valor                  |
|-------------|------------------------|
| Nombre      | `payments.dlq.exchange`|
| Tipo        | Direct               |
| Durable     | true                 |

Recibe mensajes que fallaron después de todos los reintentos.

---

## Queues

| Nombre                        | Durable | DLQ configurada          |
|-------------------------------|---------|--------------------------|
| payments.audit.queue        | true  | payments.audit.dlq     |
| payments.notification.queue | true  | payments.notification.dlq |
| payments.audit.dlq          | true  | —                        |
| payments.notification.dlq   | true  | —                        |

---

## Bindings

| Exchange             | Queue                         | Routing Key              |
|----------------------|-------------------------------|--------------------------|
| payments.exchange  | payments.audit.queue        | payment.status.changed |
| payments.exchange  | payments.notification.queue | payment.status.changed |
| payments.dlq.exchange | payments.audit.dlq       | payment.dead           |
| payments.dlq.exchange | payments.notification.dlq`| `payment.dead           |

---

## Routing Keys

| Routing Key              | Cuándo se usa                          |
|--------------------------|----------------------------------------|
| payment.status.changed | Cuando el estatus de un pago cambia    |
| payment.dead           | Cuando un mensaje falla todos los reintentos |

---

## Estructura del Mensaje

```json
{
  "paymentId":       "64f1a2b3c4d5e6f7a8b9c0d1",
  "concepto":        "Compra de un producto",
  "pagador":         "Juan Pérez",
  "beneficiario":    "Empresa Test",
  "montoTotal":      10000,
  "estatusAnterior": "PENDING",
  "estatusNuevo":    "PROCESSING",
  "fechaCambio":     "2024-01-15T10:30:00"
}