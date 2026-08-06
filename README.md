# tartis-recon-ai-parking — spot-service

## 1. Responsabilidad del microservicio

`spot-service` es el microservicio encargado de la gestión del catálogo de plazas de aparcamiento y del control de disponibilidad física del parking en el sistema **TARTIS Recon-AI**. Sus responsabilidades principales incluyen:

- **Gestión del Catálogo de Plazas:** Control del inventario de plazas y asignación por tipo de vehículo (`CAR`, `CAR_PMR` o `MOTORBIKE`).
- **Máquina de Estados de la Plaza (`SpotStatus`):**
  - `AVAILABLE` $\rightarrow$ `OCCUPIED` (ocupación atómica durante el check-in).
  - `OCCUPIED` $\rightarrow$ `AVAILABLE` (liberación atómica durante el check-out síncrono o asíncrono mediante eventos).
  - `AVAILABLE` $\rightarrow$ `UNAVAILABLE` (bloqueo por mantenimiento según **RN-10**).
  - `UNAVAILABLE` $\rightarrow$ `AVAILABLE` (desbloqueo de mantenimiento).
  - *Restricción de Estado:* La transición `OCCUPIED` $\rightarrow$ `UNAVAILABLE` está expresamente prohibida (devuelve HTTP 409 Conflict): no es posible bloquear una plaza por mantenimiento si tiene un vehículo dentro.
- **Ocupación Atómica (RN-01, RN-05):** Consulta de disponibilidad e incremento de estado atómico para evitar asignaciones duplicadas ante concurrencia de check-in.
- **Procesamiento Asíncrono e Idempotente de Eventos:** Liberación automática de la plaza al recibir eventos de cierre de estancia (`StayClosedEvent`) enviados por `stay-service` a RabbitMQ.

---

## 2. Endpoints expuestos

Todos los endpoints requieren autenticación perimetral mediante Bearer Access Token (emitido por Keycloak), exceptuando las sondas públicas de salud.

| Método | Endpoint | Descripción | Roles Autorizados (RBAC SEC-03) | Respuesta Exitosa |
|---|---|---|---|---|
| `GET` | `/v1/spots` | Listado completo de plazas y su estado actual | `ADMIN`, `OPERARIO` | `200 OK` (`List<SpotResponse>`) |
| `POST` | `/v1/spots` | Crea una nueva plaza (nace en estado `AVAILABLE`) | `ADMIN` | `201 Created` (`SpotResponse`) |
| `GET` | `/v1/spots/{id}` | Obtiene el detalle de una plaza por su UUID | `ADMIN`, `OPERARIO` | `200 OK` (`SpotResponse`) |
| `PUT` | `/v1/spots/{id}` | Actualiza la categoría de vehículo permitida en la plaza | `ADMIN` | `200 OK` (`SpotResponse`) |
| `GET` | `/v1/spots/availability` | Consulta plazas libres filtradas por tipo de vehículo (RN-01) | `ADMIN`, `OPERARIO` | `200 OK` (`SpotAvailabilityResponse`) |
| `POST` | `/v1/spots/occupy` | Ocupa una plaza libre de forma atómica (`AVAILABLE` $\rightarrow$ `OCCUPIED`) (RN-05) | `ADMIN`, `OPERARIO` | `200 OK` (`SpotResponse`) |
| `POST` | `/v1/spots/{id}/release` | Libera síncronamente una plaza ocupada (`OCCUPIED` $\rightarrow$ `AVAILABLE`) | `ADMIN`, `OPERARIO` | `200 OK` (`SpotResponse`) |
| `PATCH` | `/v1/spots/{id}/status` | Bloquea o desbloquea una plaza por mantenimiento (RN-10) | `ADMIN` | `200 OK` (`SpotResponse`) |
| `GET` | `/actuator/health` | Probes de salud del servicio (Liveness / Readiness) | Público | `200 OK` |

---

## 3. Casos de Uso (Arquitectura Hexagonal)

Los casos de uso coordinan el ciclo de vida y la persistencia de las plazas:

- **`CreateSpotUseCase`:** Crea una nueva plaza física asignando su código identificador y categoría.
- **`GetSpotAvailabilityUseCase`:** Consulta la existencia de plazas en estado `AVAILABLE` para el tipo de vehículo indicado (RN-01).
- **`GetSpotUseCase`:** Obtiene una plaza por su UUID.
- **`ListSpotsUseCase`:** Devuelve el inventario total de plazas.
- **`OccupySpotUseCase`:** Ocupa atómicamente una plaza libre (RN-05).
- **`ReleaseSpotUseCase`:** Libera la plaza pasando su estado de `OCCUPIED` a `AVAILABLE`.
- **`UpdateSpotStatusUseCase`:** Cambia el estado para mantenimiento (RN-10), impidiendo la acción si la plaza está ocupada.
- **`UpdateSpotVehicleTypeUseCase`:** Modifica el tipo de vehículo asignado a la plaza.

### Puertos de Dominio:
- **Puertos de Entrada:** REST API (`SpotRestAdapter`) y AMQP Listener (`SpotEventListenerAdapter`).
- **Puerto de Salida:** `SpotPersistence` (Implementado por `SpotPersistenceAdapter` con Spring Data JPA).

---

## 4. Eventos publicados y consumidos

### Eventos Consumidos de RabbitMQ:
- **`StayClosedEvent`:** Escucha en la cola `spot-service-stay-closed-queue` (Exchange `stay.events`, routing key `stay.closed`).
- **Mecanismos de Idempotencia:** Si la plaza ya fue liberada previamente (`SpotNotOccupiedException`) o si el evento es antiguo (`SpotEventOutdatedException`), el listener captura la excepción, registra una advertencia y devuelve confirmación (`ACK`) para no reintentar infinitamente.
- **Resiliencia & Dead Letter Queue (DLQ):** Enrutamiento con 6 reintentos exponenciales. Mensajes persistentemente fallidos son desviados a la cola muerta `spot-service-stay-closed-dlq`.

---

## 5. Variables de entorno

| Variable | Descripción | Valor por defecto (Dev) | Perfil / Uso |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfil activo de Spring Boot | `dev` | `dev` / `prod` |
| `SERVER_PORT` | Puerto HTTP del servicio | `8082` | Dev / Prod |
| `DB_HOST` | Host de la BD compartida de desarrollo | `localhost` | Dev |
| `DB_PORT` | Puerto de la BD compartida | `5432` | Dev |
| `DB_NAME` | Nombre de la BD de desarrollo | `parking_dev` | Dev |
| `SPOT_DB_HOST` | Host de la BD dedicada de plazas | `parking-spot-postgres` | Prod / Aislado |
| `SPOT_DB_PORT` | Puerto del host para la BD dedicada | `5434` (externo) / `5432` (interno) | Prod / Aislado |
| `SPOT_DB_NAME` | Nombre de la BD dedicada | `spot_db` | Prod / Aislado |
| `SPOT_DB_USER` | Usuario de la BD dedicada | `spot_user` | Prod / Aislado |
| `SPOT_DB_PASSWORD` | Contraseña de la BD dedicada | `spot_pass` | Prod / Aislado |
| `RABBITMQ_HOST` | Host del broker RabbitMQ | `rabbitmq` | Dev / Prod |
| `RABBITMQ_USER` | Usuario de autenticación RabbitMQ | `guest` | Dev / Prod |
| `RABBITMQ_PASSWORD` | Contraseña de autenticación RabbitMQ | `guest` | Dev / Prod |
| `KEYCLOAK_ISSUER_URI` | URI del emisor de Keycloak | `http://localhost:8180/realms/parking` | Dev / Prod |

---

## 6. Ejecución de forma aislada

### Opción 1: Entorno de Desarrollo (Perfil `dev`)
```bash
cd backend/spot-service
mvn spring-boot:run
```

### Opción 2: Base de Datos Dedicada (Perfil `prod` / Contenedor Aislado)
1. Arrancar la base de datos exclusiva PostgreSQL en el puerto `5434`:
   ```bash
   cd backend/spot-service
   cp .env.example .env
   docker compose up -d
   ```
2. Ejecutar la aplicación Spring Boot activando el perfil `prod` para aplicar migraciones Flyway (`V1__init.sql`, `V2__add_version.sql`):
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=prod
   ```