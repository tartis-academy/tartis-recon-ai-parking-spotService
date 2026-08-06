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

- **Eventos publicados en RabbitMQ:** Ninguno.
- **Eventos consumidos de RabbitMQ:**
  - **`StayClosedEvent`:** Escucha en la cola `spot-service-stay-closed-queue` (Exchange `stay.events`, routing key `stay.closed`). Al finalizar una estancia en `stay-service`, este servicio consume el evento para liberar la plaza asociada.
  - **Idempotencia:** Si la plaza ya fue liberada previamente (`SpotNotOccupiedException`) o el evento es obsoleto (`SpotEventOutdatedException`), se captura el evento con log de advertencia y se marca como procesado (ACK) para evitar bucles de reintento.
  - **Resiliencia & Dead Letter Queue (DLQ):** Reintentos automáticos con backoff exponencial (6 intentos). Mensajes fallidos persistentes se enrutan a la cola de mensajes muertos `spot-service-stay-closed-dlq`.

---

## 5. Variables de entorno

| Variable | Descripción | Valor por defecto (Dev) | Perfil / Uso |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfil activo de Spring Boot | `dev` | `dev` / `prod` |
| `SERVER_PORT` | Puerto HTTP del servicio | `8082` | Dev / Prod |
| `DB_HOST` | Host de la BD compartida de desarrollo | `localhost` | Dev |
| `DB_PORT` | Puerto de la BD compartida | `5432` | Dev |
| `DB_NAME` | Nombre de la BD de desarrollo | `parking_dev` | Dev |
| `DB_USER` | Usuario de la BD de desarrollo | `parking_dev` | Dev |
| `DB_PASSWORD` | Contraseña de la BD de desarrollo | `change.me` | Dev |
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

Para ejecutar y probar `spot-service` de forma independiente sin depender del resto de microservicios:

1. **Opción 1: Entorno de Desarrollo (Perfil `dev`)**
   Navegar a la carpeta del microservicio y arrancar con Maven:
   ```bash
   cd backend/spot-service
   mvn spring-boot:run
   ```
   *El servicio se conectará al esquema `spot` del Postgres compartido.*

2. **Opción 2: Base de Datos Dedicada (Perfil `prod` / Contenedores Aislados)**
   Para ejecutar contra una base de datos PostgreSQL exclusiva en puerto `5434`:
   ```bash
   cd backend/spot-service
   cp .env.example .env
   docker compose up -d
   mvn spring-boot:run -Dspring-boot.run.profiles=prod
   ```

---

## 7. Levantar el entorno local

Lo más rápido: un script que hace todos los pasos de abajo y espera a que la BD esté lista.

```bash
./setup.sh              # levanta la BD
./setup.sh down         # para el contenedor
./setup.sh clean        # para y BORRA los datos
```

Si prefieres ir a mano, los pasos son estos:

Crea la red compartida que conecta pgAdmin con los Postgres de cada servicio (solo la primera vez):
```bash
docker network create parking-shared
```

Levanta el PostgreSQL dedicado de `spot-service` en el puerto 5434:
```bash
cp .env.example .env
docker compose up -d
```

Comprueba que el contenedor esté `healthy`:
```bash
docker compose ps
```

---

## 8. Datos de conexión

| Dato | Valor |
|---|---|
| BD desde tu máquina | `localhost:5434` · `spot_db` · `spot_user` |
| BD desde pgAdmin | `parking-spot-postgres:5432` (nombre del contenedor, puerto interno) |

**pgAdmin y SonarQube no se levantan desde este repo**: son herramientas compartidas y viven en el compose de `tartis-recon-ia-parking-infra`. Levántalas desde allí y desde pgAdmin registra esta BD con los datos de la tabla.

Cada microservicio usa un puerto distinto en el host para no chocar: vehicle 5433, spot 5434, tariff 5435, ticket 5436, stay 5437.

---

## 9. Migraciones de base de datos (Flyway)

El esquema ya no se crea a mano ni con un `schema.sql` montado como init script: `V1__init.sql` (en `backend/spot-service/src/main/resources/db/migration`) es la baseline, y Flyway la aplica solo al arrancar la app contra la BD dedicada (perfil `prod`). En dev, Flyway está desactivado (`spring.flyway.enabled=false` en `application-dev.properties`): el Postgres compartido con 5 schemas sigue gestionado por `ddl-auto=update`, fuera del alcance de esta migración.

Para añadir un cambio de esquema: crea `V2__descripcion.sql` (nunca edites `V1__init.sql` una vez desplegado) en la misma carpeta, con el DDL nuevo. Flyway lo detecta y lo aplica en el siguiente arranque.

---

## 10. Escaneo de imagen (Trivy)

El job `docker-scan` de la CI construye la imagen final del Dockerfile y la escanea con [Trivy](https://trivy.dev/). El informe completo (`CRITICAL` + `HIGH`) se publica siempre en la pestaña **Security** del repo; solo una vulnerabilidad `CRITICAL` hace fallar el job.

Si una `CRITICAL` no tiene fix disponible todavía y hay que aceptar el riesgo de forma consciente, se ignora explícitamente añadiendo su CVE a un `.trivyignore` en la raíz del repo (no existe ninguno hoy).

---

## 11. Problemas frecuentes

- `network parking-shared ... not found` $\rightarrow$ te falta crear la red compartida (`docker network create parking-shared`, o `./setup.sh` en `infra`).
- El puerto 5434 ya está en uso $\rightarrow$ cámbialo en el `.env`. El puerto del host es configurable; el interno no.
- Cambias el `.env` y no se entera $\rightarrow$ `docker compose up -d --force-recreate`. Si tocas usuario o contraseña, además `docker compose down -v`: esas credenciales solo se aplican al crear la BD por primera vez.