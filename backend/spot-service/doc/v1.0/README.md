# spot-service — Alcance y Especificación de Fase 2 (v1.0.0)

Este documento especifica el alcance funcional completo, el consumo asíncrono de eventos, la seguridad y la arquitectura del microservicio `spot-service` en la **Fase 2 (v1.0.0)** del sistema de gestión de parking **TARTIS Recon-AI**.

---

## 1. Responsabilidad del Microservicio en Fase 2

En la Fase 2 (`v1.0.0`), `spot-service` integra procesamiento de eventos asíncronos en tiempo real:

- **Consumo Asíncrono de Cierre de Estancias:** `SpotEventListenerAdapter` escuchando en la cola `spot-service-stay-closed-queue` de RabbitMQ para liberar plazas automáticamente tras un check-out en `stay-service`.
- **Mecanismos de Idempotencia:** Tratamiento explícito de `SpotNotOccupiedException` y `SpotEventOutdatedException` permitiendo confirmar (ACK) eventos duplicados o fuera de orden sin errores en la cola.
- **Resiliencia & DLQ:** Reintentos exponenciales (6 intentos) y enrutamiento automático a la cola de mensajes muertos `spot-service-stay-closed-dlq`.
- **Seguridad Perimetral & RBAC (SEC-03):** Integración con Keycloak IdP y Kong API Gateway.
- **Validación Estricta de Máquina de Estados:** Prohibida la transición `OCCUPIED` $\rightarrow$ `UNAVAILABLE` (HTTP 409 Conflict si hay un vehículo dentro).

---

## 2. Componentes de Eventos Asíncronos (v1.0.0)

```text
[ stay-service ] --(publica StayClosedEvent)--> [ RabbitMQ Exchange: stay.events ]
                                                             |
                                                             v (Routing key: stay.closed)
                                            [ spot-service-stay-closed-queue ]
                                                             |
                                                             v
                                            [ SpotEventListenerAdapter (@RabbitListener) ]
                                                             |
                                                             v
                                            [ ReleaseSpotUseCase -> spot_db ]
```

---

## 3. Matriz de Endpoints REST & Seguridad RBAC (v1.0.0)

| Método HTTP | Endpoint | Descripción | Rol Keycloak Requerido |
|---|---|---|---|
| `GET` | `/v1/spots` | Listado de plazas | `ADMIN`, `OPERARIO` |
| `POST` | `/v1/spots` | Creación de plaza | `ADMIN` |
| `GET` | `/v1/spots/availability` | Disponibilidad por tipo | `ADMIN`, `OPERARIO` |
| `POST` | `/v1/spots/occupy` | Ocupación atómica de plaza | `ADMIN`, `OPERARIO` |
| `POST` | `/v1/spots/{id}/release` | Liberación síncrona de plaza | `ADMIN`, `OPERARIO` |
| `PATCH` | `/v1/spots/{id}/status` | Bloqueo por mantenimiento | `ADMIN` |
