# Alcance de la Fase I (MVP v0.5.0) — spot-service

Documento explicativo del alcance, responsabilidad, modelo de dominio, endpoints expuestos e infraestructura del microservicio `spot-service` durante la **Fase I (MVP v0.5.0)** del sistema de parking inteligente **TARTIS Recon-AI**.

---

## 1. Responsabilidad del Microservicio en Fase I

En la Fase I, `spot-service` se encarga de la gestión síncrona del inventario de plazas del parking:
- **Gestión de Inventario de Plazas:** Alta, listado y consulta de plazas clasificadas por tipo de vehículo.
- **Consulta de Disponibilidad (RN-01):** Verificación de plazas libres disponibles para asignación.
- **Ocupación Atómica de Plaza (RN-05):** Reserva y marcado síncrono de plaza a `OCCUPIED` durante el check-in.
- **Liberación Síncrona de Plaza:** Transición de la plaza a `AVAILABLE` invocada síncronamente en check-out.
- **Bloqueo por Mantenimiento (RN-10):** Transición de plaza a `UNAVAILABLE` para tareas de conservación.

---

## 2. Modelo de Dominio (Fase I)

Entidad principal **`Spot`** con los siguientes atributos:

| Atributo | Tipo | Descripción | Validación / Restricción |
|---|---|---|---|
| `id` | `UUID` | Identificador único universal de la plaza | Autogenerado (PK) |
| `spotNumber` | `String` | Código/Identificador físico de la plaza | Único, no nulo |
| `vehicleType` | `VehicleType` | Tipo de vehículo admitido (`CAR`, `MOTORBIKE`, `CAR_PMR`) | No nulo |
| `status` | `SpotStatus` | Estado de la plaza (`AVAILABLE`, `OCCUPIED`, `UNAVAILABLE`) | No nulo |

---

## 3. Endpoints REST Expuestos (Fase I)

| Método HTTP | Endpoint | Descripción | Cuerpo / Parámetros | Respuesta Éxito |
|---|---|---|---|---|
| `GET` | `/v1/spots` | Listado completo de plazas de aparcamiento | Ninguno | `200 OK` (Lista de `SpotResponse`) |
| `POST` | `/v1/spots` | Creación de una nueva plaza (nace `AVAILABLE`) | JSON `CreateSpotRequest` | `201 Created` (`SpotResponse`) |
| `GET` | `/v1/spots/availability` | Consulta de disponibilidad por categoría (**RN-01**) | `vehicleType` | `200 OK` (`SpotAvailabilityResponse`) |
| `POST` | `/v1/spots/occupy` | Ocupación atómica de plaza (**RN-05**) | JSON `OccupySpotRequest` | `200 OK` (`SpotResponse`) |
| `POST` | `/v1/spots/{id}/release` | Liberación síncrona de plaza | `{id}` (UUID) | `200 OK` (`SpotResponse`) |
| `PATCH` | `/v1/spots/{id}/status` | Bloqueo o cambio por mantenimiento (**RN-10**) | `{id}`, JSON `UpdateSpotStatusRequest` | `200 OK` (`SpotResponse`) |

---

## 4. Arquitectura y Persistencia en Fase I

- **Arquitectura Hexagonal (Puertos y Adaptadores):** Adaptador de entrada REST (`SpotRestControllerAdapter`), Casos de Uso (`OccupySpotUseCase`, `ReleaseSpotUseCase`), Adaptador de salida JPA (`SpotPersistenceAdapter`).
- **Base de Datos:** PostgreSQL compartido `parking_dev` en puerto `5432`, esquema `spot`.

---

## 5. Diferencias Clave respecto a la Fase II (v1.0.0)

1. **Liberación Asíncrona por Eventos:** No existe el consumidor `@RabbitListener` de `StayClosedEvent`.
2. **Resiliencia & Dead Letter Queue:** No existen colas de mensajes ni colas DLQ (`spot-service-stay-closed-dlq`).
3. **Seguridad OAuth2 / Keycloak & Kong:** Sin verificación de tokens JWT ni roles RBAC (`ADMIN`, `OPERARIO`).
4. **Máquina de Estados Estricta:** No existe el bloqueo HTTP 409 al intentar pasar `OCCUPIED` $\rightarrow$ `UNAVAILABLE`.
