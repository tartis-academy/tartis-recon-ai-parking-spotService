# spot-service — Alcance y Especificación de Fase 1 (v0.5.0)

Este documento especifica el alcance funcional, el modelo de datos y los endpoints del microservicio `spot-service` correspondientes a la **Fase 1 (MVP - v0.5.0)** del sistema de gestión de parking **TARTIS Recon-AI**.

---

## 1. Responsabilidad del Microservicio en Fase 1

En la Fase 1 (`v0.5.0`), `spot-service` actúa como el **gestor síncrono del inventario y estado de plazas** del parking:

- **Gestión de Plazas:** Creación, listado y cambio de estado de plazas físicas.
- **Consulta de Disponibilidad (RN-01):** Búsqueda síncrona de plazas libres (`AVAILABLE`) filtradas por tipo de vehículo (`CAR`, `CAR_PMR`, `MOTORBIKE`).
- **Ocupación Atómica (RN-05):** Reserva e incremento de estado a `OCCUPIED` al procesar un check-in.
- **Liberación Síncrona & Mantenimiento (RN-10):** Liberación directa a `AVAILABLE` y bloqueo de plazas por mantenimiento (`UNAVAILABLE`).

---

## 2. Modelo de Dominio (`Spot`) en Fase 1

### Atributos de la Entidad `Spot`

| Atributo | Tipo Java | Descripción | Obligatorio |
|---|---|---|---|
| `id` | `UUID` | Identificador único de la plaza | Sí |
| `identifier` | `String` | Código físico/visual de la plaza (ej: `A-101`) | Sí (Único) |
| `status` | `SpotStatus` | Estado (`AVAILABLE`, `OCCUPIED`, `UNAVAILABLE`) | Sí |
| `vehicleType` | `VehicleType` | Categoría permitida (`CAR`, `CAR_PMR`, `MOTORBIKE`) | Sí |

---

## 3. Endpoints REST Expuestos en Fase 1 (v0.5.0)

| Método HTTP | Endpoint | Descripción | Respuesta Exitosa |
|---|---|---|---|
| `GET` | `/v1/spots` | Listado general de plazas | `200 OK` (`List<SpotResponse>`) |
| `POST` | `/v1/spots` | Creación de una plaza (nace `AVAILABLE`) | `201 Created` (`SpotResponse`) |
| `GET` | `/v1/spots/availability` | Consulta de disponibilidad por tipo (RN-01) | `200 OK` (`SpotAvailabilityResponse`) |
| `POST` | `/v1/spots/occupy` | Ocupación atómica de plaza (RN-05) | `200 OK` (`SpotResponse`) |
| `POST` | `/v1/spots/{id}/release` | Liberación síncrona de plaza | `200 OK` (`SpotResponse`) |
| `PATCH` | `/v1/spots/{id}/status` | Bloqueo por mantenimiento (RN-10) | `200 OK` (`SpotResponse`) |

---

## 4. Persistencia PostgreSQL — Baseline (`V1__init.sql`)

```sql
CREATE SCHEMA IF NOT EXISTS spot;

CREATE TABLE spot.spots (
    id UUID PRIMARY KEY,
    identifier VARCHAR(20) NOT NULL CONSTRAINT uk_spot_identifier UNIQUE,
    status VARCHAR(20) NOT NULL,
    vehicle_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

---

## 5. Exclusiones de la Fase 1 (Diferencias con Fase 2 / v1.0.0)

- ❌ **Sin consumo asíncrono de RabbitMQ:** En Fase 1 no existía `SpotEventListenerAdapter` ni la cola `spot-service-stay-closed-queue`.
- ❌ **Sin Dead Letter Queue (DLQ):** Sin resiliencia por eventos de mensajería muerta.
- ❌ **Sin autenticación Keycloak ni RBAC (SEC-03):** Peticiones abiertas sin JWT.
- ❌ **Sin Kong API Gateway:** Invocaciones directas al puerto `8082`.
- ❌ **Sin concurrencia optimista:** Sin columna `version` (`V2__add_version.sql`).
- ❌ **Sin trazabilidad MDC (GW-06).**
