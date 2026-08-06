# spot-service — Alcance y Especificación de Fase 2 (v1.0.0)

Este documento especifica el alcance funcional completo, el modelo de datos, los componentes asíncronos y la seguridad del microservicio `spot-service` correspondientes a la **Fase 2 (v1.0.0)** del sistema **TARTIS Recon-AI**.

---

## 1. Responsabilidad del Microservicio en Fase 2

En la Fase 2 (`v1.0.0`), `spot-service` incorpora la **liberación asíncrona de plazas guiada por eventos**:

- **Consumo Asíncrono de `StayClosedEvent`:** Implementación de `SpotEventListenerAdapter` con `@RabbitListener` escuchando en la cola `spot-service-stay-closed-queue` para pasar automáticamente la plaza de `OCCUPIED` a `AVAILABLE` al realizar check-out en `stay-service`.
- **Mecanismos de Idempotencia en Eventos:** Tratamiento explícito de `SpotNotOccupiedException` y `SpotEventOutdatedException` permitiendo procesar (ACK) eventos duplicados sin fallar la cola.
- **Resiliencia & Dead Letter Queue (DLQ):** Enrutamiento de eventos fallidos tras 6 reintentos exponenciales a `spot-service-stay-closed-dlq`.
- **Seguridad & RBAC (SEC-03):** Integración con **Keycloak IdP** y **Kong API Gateway** con restricción de acceso por roles (`ADMIN` para administración/mantenimiento; `ADMIN`/`OPERARIO` para consultas y liberación).
- **Control de Concurrencia Optimista:** Columna `version` en base de datos para prevenir colisiones en cambios de estado de plaza.

---

## 2. Componentes de Entrada & Salida (v1.0.0)

### Adaptador Listener por Eventos (AMQP)
- **`SpotEventListenerAdapter`:** Escucha mensajes de la cola `spot-service-stay-closed-queue` enviada desde la Exchange `stay.events`.
- **Regla de Máquina de Estados:** Prohibida la transición de `OCCUPIED` $\rightarrow$ `UNAVAILABLE` (bloqueo por mantenimiento) devolviendo HTTP `409 Conflict` si la plaza está ocupada.

---

## 3. Matriz de Endpoints REST & Seguridad RBAC (v1.0.0)

| Método HTTP | Endpoint | Descripción | Roles Permitidos (RBAC) | Respuesta Exitosa |
|---|---|---|---|---|
| `GET` | `/v1/spots` | Listado general de plazas | `ADMIN`, `OPERARIO` | `200 OK` |
| `POST` | `/v1/spots` | Creación de plaza (`AVAILABLE`) | `ADMIN` | `201 Created` |
| `GET` | `/v1/spots/availability` | Disponibilidad por categoría (RN-01) | `ADMIN`, `OPERARIO` | `200 OK` |
| `POST` | `/v1/spots/occupy` | Ocupación atómica de plaza (RN-05) | `ADMIN`, `OPERARIO` | `200 OK` |
| `POST` | `/v1/spots/{id}/release` | Liberación de plaza | `ADMIN`, `OPERARIO` | `200 OK` |
| `PATCH` | `/v1/spots/{id}/status` | Bloqueo por mantenimiento (RN-10) | `ADMIN` | `200 OK` |

---

## 4. Persistencia PostgreSQL (Migraciones Flyway)

### `V1__init.sql` (Baseline) & `V2__add_version.sql` (Concurrencia)
```sql
ALTER TABLE spot.spots ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
```
En entorno `prod`, se ejecuta sobre base de datos dedicada `spot_db` en puerto `5434`.
