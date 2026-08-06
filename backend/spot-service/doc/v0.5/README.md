# spot-service — Alcance y Especificación de Fase 1 (v0.5.0)

Este documento especifica el alcance funcional, el modelo de datos y los endpoints del microservicio `spot-service` correspondientes a la **Fase 1 (MVP - v0.5.0)** del sistema de gestión de parking **TARTIS Recon-AI**.

---

## 1. Responsabilidad del Microservicio en Fase 1

En la Fase 1 (`v0.5.0`), `spot-service` actúa como el **catálogo síncrono de plazas de aparcamiento**:

- **Gestión de Plazas (`Spot`):** Registro de plazas, consulta de inventario y estado.
- **Control de Disponibilidad (RN-01):** Verificación síncrona de plazas libres por categoría (`CAR`, `CAR_PMR`, `MOTORBIKE`).
- **Ocupación y Liberación Síncrona (RN-05):** Ocupación atómica de plaza al realizar el check-in y liberación síncrona mediante llamada HTTP REST.
- **Bloqueo por Mantenimiento (RN-10):** Transición manual de plazas a estado `UNAVAILABLE`.

---

## 2. Modelo de Dominio (`Spot`) en Fase 1

| Atributo | Tipo Java | Descripción | Obligatorio |
|---|---|---|---|
| `id` | `UUID` | Identificador único de la plaza | Sí |
| `number` | `String` | Código/Número de la plaza (ej: "A-101") | Sí (Único) |
| `type` | `VehicleType` | Categoría de vehículo admitida (`CAR`, `CAR_PMR`, `MOTORBIKE`) | Sí |
| `status` | `SpotStatus` | Estado de la plaza (`AVAILABLE`, `OCCUPIED`, `UNAVAILABLE`) | Sí |

---

## 3. Endpoints REST Expuestos en Fase 1 (v0.5.0)

| Método HTTP | Endpoint | Descripción | Respuesta Exitosa |
|---|---|---|---|
| `GET` | `/v1/spots` | Listado general de plazas | `200 OK` (`List<SpotResponse>`) |
| `POST` | `/v1/spots` | Alta de nueva plaza (nace `AVAILABLE`) | `201 Created` (`SpotResponse`) |
| `GET` | `/v1/spots/availability` | Consulta de disponibilidad por tipo (RN-01) | `200 OK` (`SpotAvailabilityResponse`) |
| `POST` | `/v1/spots/occupy` | Ocupación atómica de plaza (RN-05) | `200 OK` (`SpotResponse`) |
| `POST` | `/v1/spots/{id}/release` | Liberación síncrona de plaza | `200 OK` (`SpotResponse`) |
| `PATCH` | `/v1/spots/{id}/status` | Bloqueo por mantenimiento (RN-10) | `200 OK` (`SpotResponse`) |

---

## 4. Exclusiones de la Fase 1 (Novedades de Fase 2 / v1.0.0)

- ❌ **Sin consumo asíncrono de RabbitMQ:** En Fase 1 no existía la escucha del evento `StayClosedEvent`.
- ❌ **Sin colas de mensajes muertos (DLQ):** Sin `spot-service-stay-closed-dlq`.
- ❌ **Sin autenticación Keycloak / RBAC (`SEC-03`).**
- ❌ **Sin enrutamiento por Kong API Gateway.**
- ❌ **Sin control de concurrencia optimista (`version`).**
