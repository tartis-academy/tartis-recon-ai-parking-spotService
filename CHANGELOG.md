# Changelog

All notable changes to the `spot-service` microservice will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-08-06

### Added
- **Consumo Asíncrono de Eventos de Cierre (`StayClosedEvent`):** Implementado `SpotEventListenerAdapter` con `@RabbitListener` escuchando en `spot-service-stay-closed-queue` para la liberación automática de plazas tras el check-out en `stay-service`.
- **Mecanismos de Idempotencia en Eventos:** Tratamiento explícito de `SpotNotOccupiedException` y `SpotEventOutdatedException` permitiendo procesar (ACK) eventos duplicados o desfasados sin fallar la cola ni reintentar infinitamente.
- **Resiliencia & Dead Letter Queue (DLQ):** Configuración de reintentos exponenciales (6 intentos) y enrutamiento automático a la cola de mensajes muertos `spot-service-stay-closed-dlq`.
- **Integración con Keycloak & Spring Security:** OAuth2 Resource Server para validación de Bearer Access Tokens emitidos por Keycloak.
- **Enrutamiento por API Gateway (Kong):** Enrutamiento centralizado y comprobación de seguridad en el perímetro vía Kong.
- **Trazabilidad Distribuida & Logging (GW-06):** Inclusión de `CorrelationIdFilter`, `RequestIdentityFilter` y `RequestLoggingFilter` inyectando `correlationId`, `userName` y `clientId` en el MDC.
- **Control de Concurrencia Optimista:** Migración Flyway añadiendo control de versión para evitar sobreescrituras en estados de plaza.

### Changed
- **Formato Común de Errores (SEC-11 / RFC 7807):** Estandarización de respuestas de error devolviendo `ProblemDetail` / `ErrorResponse` uniforme.
- **Control de Acceso basado en Roles (RBAC):** Restricción de endpoints según matriz `SEC-03` (`ADMIN` para creación/actualización de plazas; `ADMIN`/`OPERARIO` para consulta, ocupación y liberación).
- **Base de Datos Dedicada:** Perfil `prod` con PostgreSQL dedicada en puerto 5434.

### Fixed
- **Validación de Máquina de Estados:** Prohibida la transición `OCCUPIED` -> `UNAVAILABLE` (bloqueo por mantenimiento) devolviendo HTTP 409 si la plaza tiene un vehículo dentro.
- **Manejo de Respuestas de Autenticación (401 / 403):** Emisión de cabecera `WWW-Authenticate` en respuestas 401 no autorizadas.

### Security
- **Protección con `@PreAuthorize`:** Control de acceso en controladores REST.
- **Escaneo Continuo de Vulnerabilidades:** Pipeline CI/CD integrado con Trivy (`docker-scan`).

## [0.5.0] - 2026-07-29

### Added
- **MVP Inicial de `spot-service`:** Implementación inicial de la arquitectura hexagonal para la gestión de plazas.
- **Soporte para Plazas PMR:** Integración de la categoría `CAR_PMR` en el enum `VehicleType` y en la reserva/validación de plazas libres.
- **Endpoints REST Síncronos:**
  - `GET /v1/spots`: Listado de plazas.
  - `POST /v1/spots`: Creación de plaza (nace `AVAILABLE`).
  - `GET /v1/spots/availability`: Consulta de disponibilidad de plazas por tipo de vehículo (RN-01).
  - `POST /v1/spots/occupy`: Ocupación atómica de plaza (RN-05).
  - `POST /v1/spots/{id}/release`: Liberación síncrona de plaza.
  - `PATCH /v1/spots/{id}/status`: Bloqueo por mantenimiento (RN-10).
- **Persistencia PostgreSQL:** Configuración JPA con esquema `spot`.
- **Contrato OpenAPI:** Especificación en `openapi.yml`.

[1.0.0]: https://github.com/tartis-academy/tartis-recon-ai-parking-spotService/compare/v0.5.0...v1.0.0
[0.5.0]: https://github.com/tartis-academy/tartis-recon-ai-parking-spotService/releases/tag/v0.5.0
