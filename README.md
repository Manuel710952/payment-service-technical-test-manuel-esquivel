# Payment Service

Servicio de pagos desarrollado para el ejercicio de prueba técnica desarrollador Sr.

Autor: Manuel Esquivel Alemán

-----

## Tecnologías utilizadas

- Java 17 
- Spring Boot 3.2 
- Spring Data MongoDB 
- Spring AMQP / RabbitMQ
- Springdoc OpenAPI (Swagger) 
- JaCoCo 
- JUnit 5 + Mockito 
- Testcontainers 
- Docker + Docker Compose 

-----

## Requisitos

- Docker Desktop instalado y corriendo
- Java 17
- Maven 3.9+

-----

## Levantar el proyecto
```Bash
# Clonar el repositorio
git clone https://github.com/Manuel710952/payment-service-technical-test-manuel-esquivel.git
cd payment-service

# Levantar todos los servicios (MongoDB + RabbitMQ + App)
docker compose up --build
```

|Servicio   |URL                                        |
|-----------|-------------------------------------------|
|API REST   |http://localhost:8080/api/v1               |
|Swagger UI |http://localhost:8080/swagger-ui/index.html|
|RabbitMQ UI|http://localhost:15672                     |
|MongoDB    |mongodb://localhost:27017                  |

Credenciales RabbitMQ: usuarioTest / 1234

-----

## Endpoints

|Método|Endpoint                      |Descripción           |
|------|------------------------------|----------------------|
|POST  |`/api/v1/payments`            |Crear pago            |
|GET   |`/api/v1/payments`            |Listar todos los pagos|
|GET   |`/api/v1/payments/{id}`       |Obtener pago por ID   |
|GET   |`/api/v1/payments/{id}/status`|Consultar estatus     |
|PATCH |`/api/v1/payments/{id}/status`|Cambiar estatus       |

-----

## Transiciones de estatus permitidas
```
PENDING to PROCESSING
PENDING to FAILED
PROCESSING to COMPLETED
PROCESSING to FAILED
COMPLETED to (estado terminal, no permite cambios)
FAILED    to (estado terminal, no permite cambios)
```
-----

## Carpeta db — Inicialización de MongoDB

La carpeta src/main/resources/db/ contiene los scripts que MongoDB ejecuta
automáticamente al iniciar el contenedor por primera vez.

mongo-init.js crea la colección payments con validación de esquema JSON
directamente en la base de datos, garantizando integridad de datos a nivel de
motor, no solo a nivel de aplicación. Esto incluye:

- Tipos de datos correctos por campo (string, int, decimal, date)
- Campos obligatorios definidos explícitamente
- Valores permitidos para el campo estatus (enum a nivel de BD)
- Índices para optimizar consultas frecuentes por estatus, pagador,
  beneficiario y fechaCreacion

payment-schema.json es la documentación del esquema en formato JSON
para revicion de equipo tecnico (solicitado en al descripcion del ejercicio practico).

-----

## Perfiles de ambiente

|Perfil|Activación                   |Nivel de logs                          |
|------|-----------------------------|---------------------------------------|
|`dev` |Por defecto                  |DEBUG — logs detallados para desarrollo|
|`qa`  |`SPRING_PROFILES_ACTIVE=qa`  |INFO — flujo normal                    |
|`prod`|`SPRING_PROFILES_ACTIVE=prod`|WARN — solo errores importantes        |

Para cambiar el perfil en Docker, edita esta línea en docker-compose.yml:
```YAML
SPRING_PROFILES_ACTIVE: dev
```
-----

## Ejecutar tests

### Tests unitarios (sin Docker)
```Bash
mvn test -Dtest=PaymentServiceTest,PaymentControllerTest
```

### Tests de integración (requiere Docker Desktop corriendo)
```Bash
mvn test -Dtest=PaymentIntegrationTest -DrunIntegrationTests=true
```

### Todos los tests
```Bash
mvn test -DrunIntegrationTests=true
```
-----

## testing — observaciones

Tests unitarios del Service (`PaymentServiceTest`) — usan Mockito para
aislar el PaymentService de sus dependencias (repositorio y publisher).

Tests unitarios del Controller (`PaymentControllerTest`) — usan MockMvc
para simular peticiones HTTP sin levantar un servidor real. Verifican que
los endpoints responden con los códigos HTTP correctos y el formato de
respuesta esperado.

Tests de integración (`PaymentIntegrationTest`) — usan Testcontainers
en lugar de mocks para MongoDB y RabbitMQ. Testcontainers levanta contenedores
Docker reales durante la ejecución del test y los destruye al terminar.

La decisión de usar Testcontainers en lugar de mocks para la capa de
integración fue porque investigando, los mocks de MongoDB no
detectan problemas de validación de esquema, índices, o comportamiento del
driver. Testcontainers si hace que las pruebas reflejan el comportamiento
real del sistema, evitando falsos positivos que solo aparecerían
en el ambiente real.

-----

## Reporte de cobertura JaCoCo

Después de correr los tests, JaCoCo genera un reporte HTML:
```Bash
mvn test
```

Abre el reporte en el navegador:
```
target/site/jacoco/index.html
```
-----

## Documentación de la API — Swagger UI

Con el proyecto corriendo, accede a:
```
http://localhost:8080/swagger-ui/index.html
```

Para ver la docuemntacion de los endpoints.

-----

## Arquitectura de mensajería RabbitMQ
Política de reintentos: 3 intentos.
Después del tercer fallo el mensaje va a la DLQ para análisis posterior.

-----

## Importar colección Postman

Importar payments.postman_collection.json en Postman.