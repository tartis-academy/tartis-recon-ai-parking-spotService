# Alcance de la Fase II (v1.0.0) — spot-service

Documento explicativo del alcance, responsabilidad, modelo de dominio, endpoints expuestos, mensajería asíncrona, seguridad e infraestructura del microservicio `spot-service` durante la **Fase II (v1.0.0)** del sistema de parking inteligente **TARTIS Recon-AI**.

---

## 1. Responsabilidad del Microservicio en Fase II

En la Fase II, `spot-service` evoluciona incorporando integración asíncrona por eventos, seguridad centralizada y resiliencia:
- **Consumo Asíncrono de Eventos de Cierre (`StayClosedEvent`):** Implementación del listener `SpotEventListenerAdapter` con `@RabbitListener` escuchando en `spot-service-stay-closed-queue` para liberar automáticamente plazas tras check-out en `stay-service`.
- **Mecanismos de Idempotencia en Eventos:** Manejo explícito de `SpotNotOccupiedException` y `SpotEventOutdatedException` permitiendo confirmar (ACK) eventos desfasados o duplicados sin fallar la cola.
- **Resiliencia & Dead Letter Queue (DLQ):** Reintentos exponenciales (6 intentos) y desvío automático a `spot-service-stay-closed-dlq`.
- **Soporte para Plazas PMR:** Añadida la categoría `CAR_PMR` en el enum `VehicleType` y en la validación de disponibilidad.
- **Validación de Máquina de Estados Estricta:** Prohibición de la transición `OCCUPIED` $\rightarrow$ `UNAVAILABLE` devolviendo HTTP 409 si la plaza está ocupada por un vehículo.
- **Seguridad OAuth2 / Keycloak & Kong:** Resource Server para validación JWT y roles RBAC (`ADMIN`, `OPERARIO`).

---

## 2. Modelo de Dominio y Persistencia (Fase II)

Entidad **`Spot`**:

| Atributo | Tipo | Descripción | Validación / Restricción |
|---|---|---|---|
| `id` | `UUID` | Identificador único universal de la plaza | Autogenerado (PK) |
| `spotNumber` | `String` | Código/Identificador físico de la plaza | Único, no nulo |
| `vehicleType` | `VehicleType` | Tipo de vehículo (`CAR`, `MOTORBIKE`, `VAN`, `CAR_PMR`) | No nulo |
| `status` | `SpotStatus` | Estado de la plaza (`AVAILABLE`, `OCCUPIED`, `UNAVAILABLE`) | No nulo |
| `version` | `Long` | Control de concurrencia optimista | Flyway `V2` |

---

## 3. Endpoints Expuestos y Eventos Consumidos (Fase II)

### API REST:
| Método HTTP | Endpoint | Descripción | Rol Requerido |
|---|---|---|---|
| `GET` | `/v1/spots` | Listado completo de plazas | `ADMIN`, `OPERARIO` |
| `POST` | `/v1/spots` | Creación de nueva plaza (`AVAILABLE`) | `ADMIN` |
| `GET` | `/v1/spots/availability` | Consulta de disponibilidad por categoría (**RN-01**) | `ADMIN`, `OPERARIO` |
| `POST` | `/v1/spots/occupy` | Ocupación atómica de plaza (**RN-05**) | `ADMIN`, `OPERARIO` |
| `POST` | `/v1/spots/{id}/release` | Liberación manual de plaza | `ADMIN`, `OPERARIO` |
| `PATCH` | `/v1/spots/{id}/status` | Bloqueo por mantenimiento (**RN-10**) | `ADMIN` |

### Consumo Asíncrono de Eventos AMQP:
- **Cola Consumida:** `spot-service-stay-closed-queue`
- **Evento Procesado:** `StayClosedEvent`
- **Acción:** Liberar la plaza `spotId` asignada cambiando estado de `OCCUPIED` a `AVAILABLE`.

---

## 4. Arquitectura y Seguridad (Fase II)

- **Adaptadores de Entrada Duales:** REST Controller Adapter y Event Listener Adapter (`SpotEventListenerAdapter`).
- **Seguridad a nivel de Método:** `@PreAuthorize` con `KeycloakRoleConverter`.
- **Base de Datos y Migraciones:** Postgres dedicado `spot_db` en puerto `5434` (perfil `prod`), migraciones Flyway `V1__init.sql` y `V2`.
- **Formato Común de Errores RFC 7807 (SEC-11):** Respuestas de error estandarizadas devolviendo `ProblemDetail` / `ErrorResponse`.
