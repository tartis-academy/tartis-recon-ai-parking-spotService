

# tartis-recon-ai-parking — spot-service

## Responsabilidad del microservicio

`spot-service` es el microservicio encargado de la gestión de plazas de aparcamiento y disponibilidad física del parking. Sus responsabilidades principales incluyen:
- **Gestión del Catálogo de Plazas:** Control del inventario de plazas y su tipo de vehículo asignado (`CAR`, `CAR_PMR` o `MOTORBIKE`).
- **Máquina de Estados de la Plaza (`SpotStatus`):**
  - `AVAILABLE` -> `OCCUPIED` (ocupación atómica durante el check-in).
  - `OCCUPIED` -> `AVAILABLE` (liberación atómica durante el check-out o asíncrona mediante eventos).
  - `AVAILABLE` -> `UNAVAILABLE` (bloqueo por mantenimiento, según **RN-10**).
  - `UNAVAILABLE` -> `AVAILABLE` (desbloqueo de mantenimiento).
  - *Nota:* La transición `OCCUPIED` -> `UNAVAILABLE` está prohibida (devuelve HTTP 409): no se puede bloquear por mantenimiento una plaza con vehículo dentro.
- **Ocupación Atómica:** Asignación atómica de plazas según **RN-01** y **RN-05** para evitar que dos entradas simultáneas ocupen la misma plaza.
- **Procesamiento Asíncrono de Eventos:** Liberación automática de plazas al recibir eventos de cierre de estancia (`StayClosedEvent`) desde RabbitMQ.

## Endpoints expuestos

Todos los endpoints requieren autenticación mediante Bearer Token (Access Token emitido por Keycloak), exceptuando el endpoint público de salud.

| Método | Endpoint | Descripción | Roles Autorizados |
|---|---|---|---|
| `GET` | `/v1/spots` | Listado completo de plazas y su estado actual | `ADMIN`, `OPERARIO` |
| `POST` | `/v1/spots` | Crea una nueva plaza (nace en estado `AVAILABLE`) | `ADMIN` |
| `GET` | `/v1/spots/{id}` | Obtiene el detalle de una plaza por su UUID | `ADMIN`, `OPERARIO` |
| `PUT` | `/v1/spots/{id}` | Actualiza el tipo de vehículo permitido en la plaza | `ADMIN` |
| `GET` | `/v1/spots/availability` | Consulta plazas libres por tipo de vehículo (RN-01) | `ADMIN`, `OPERARIO` |
| `POST` | `/v1/spots/occupy` | Ocupa una plaza libre de forma atómica (`AVAILABLE` -> `OCCUPIED`) | `ADMIN`, `OPERARIO` |
| `POST` | `/v1/spots/{id}/release` | Libera una plaza ocupada (`OCCUPIED` -> `AVAILABLE`) | `ADMIN`, `OPERARIO` |
| `PATCH` | `/v1/spots/{id}/status` | Bloquea/desbloquea plaza por mantenimiento (`AVAILABLE` <-> `UNAVAILABLE`) | `ADMIN`, `OPERARIO` |
| `GET` | `/actuator/health` | Probes de salud del servicio (Liveness / Readiness) | Público |

## Eventos publicados y consumidos

Este microservicio combina operaciones REST síncronas con consumo de eventos asíncronos en RabbitMQ.

- **Eventos publicados en RabbitMQ:** Ninguno.
- **Eventos consumidos de RabbitMQ:**
  - **`StayClosedEvent`:** Escucha en la cola `spot-service-stay-closed-queue` (Exchange `stay.events`, routing key `stay.closed`). Al finalizar una estancia en `stay-service`, este servicio consume el evento para liberar la plaza asociada.
  - **Idempotencia:** Si la plaza ya fue liberada previamente (`SpotNotOccupiedException`) o el evento es obsoleto (`SpotEventOutdatedException`), se captura el evento con log de advertencia y se marca como procesado (ACK) para evitar bucles de reintento.
  - **Resiliencia & Dead Letter Queue (DLQ):** Reintentos automáticos con backoff exponencial (6 intentos). Mensajes fallidos persistentes se enrutan a la cola de mensajes muertos `spot-service-stay-closed-dlq`.

## Variables de entorno

| Variable | Descripción | Valor por defecto (Dev) | Perfil / Uso |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfil activo de Spring Boot | `dev` | `dev` / `prod` |
| `DB_HOST` | Host de la BD compartida de desarrollo | `localhost` | Dev |
| `DB_PORT` | Puerto de la BD compartida | `5432` | Dev |
| `DB_NAME` | Nombre de la BD de desarrollo | `parking_dev` | Dev |
| `DB_USER` | Usuario de la BD de desarrollo | `parking_dev` | Dev |
| `DB_PASSWORD` | Contraseña de la BD de desarrollo | `change.me` | Dev |
| `SPOT_DB_HOST` | Host de la BD dedicada de plazas | — | Prod / Aislado |
| `SPOT_DB_PORT` | Puerto de la BD dedicada de plazas | `5432` | Prod / Aislado |
| `SPOT_DB_NAME` | Nombre de la BD dedicada | `spot_db` | Prod / Aislado |
| `SPOT_DB_USER` | Usuario de la BD dedicada | — | Prod / Aislado |
| `SPOT_DB_PASSWORD` | Contraseña de la BD dedicada | — | Prod / Aislado |
| `RABBITMQ_HOST` | Host del broker RabbitMQ | `rabbitmq` | Dev / Prod |
| `RABBITMQ_USER` | Usuario de autenticación RabbitMQ | `guest` | Dev / Prod |
| `RABBITMQ_PASSWORD` | Contraseña de autenticación RabbitMQ | `guest` | Dev / Prod |
| `KEYCLOAK_ISSUER_URI` | URI del emisor de Keycloak (Issuer URI) | `http://localhost:8180/realms/parking` | Dev / Prod |

## Ejecución de forma aislada

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

## Levantar el entorno local

Lo más rápido: un script que hace todos los pasos de abajo y espera a que la BD esté lista.

```bash
./setup.sh              # levanta la BD
./setup.sh down         # para el contenedor
./setup.sh clean        # para y BORRA los datos
```

Si prefieres ir a mano, los pasos son estos.

Crea la red compartida que conecta pgAdmin con los Postgres de cada servicio (solo la primera vez).

```bash
docker network create parking-shared
```

Levanta el PostgreSQL dedicado de spot-service en el puerto 5434.

```bash
cp .env.example .env
docker compose up -d
```

Comprueba que el contenedor esté `healthy`.

```bash
docker compose ps
```

## Datos de conexión

| Dato | Valor |
|---|---|
| BD desde tu máquina | `localhost:5434` · `spot_db` · `spot_user` |
| BD desde pgAdmin | `parking-spot-postgres:5432` (nombre del contenedor, puerto interno) |

**pgAdmin y SonarQube no se levantan desde este repo**: son herramientas compartidas y viven en el compose de `tartis-recon-ia-parking-infra`. Levántalas desde allí y desde pgAdmin registra esta BD con los datos de la tabla.

Cada microservicio usa un puerto distinto en el host para no chocar: vehicle 5433, spot 5434, tariff 5435, ticket 5436, stay 5437.

## Migraciones de base de datos (Flyway)

El esquema ya no se crea a mano ni con un `schema.sql` montado como init
script: `V1__init.sql` (en `backend/spot-service/src/main/resources/db/migration`)
es la baseline, y Flyway la aplica solo al arrancar la app contra la BD
dedicada (perfil `prod`). En dev, Flyway está desactivado
(`spring.flyway.enabled=false` en `application-dev.properties`): el Postgres
compartido con 5 schemas sigue gestionado por `ddl-auto=update`, fuera del
alcance de esta migración.

Para añadir un cambio de esquema: crea `V2__descripcion.sql` (nunca edites
`V1__init.sql` una vez desplegado) en la misma carpeta, con el DDL nuevo.
Flyway lo detecta y lo aplica en el siguiente arranque.

## Escaneo de imagen (Trivy)

El job `docker-scan` de la CI construye la imagen final del Dockerfile y la
escanea con [Trivy](https://trivy.dev/). El informe completo (`CRITICAL` +
`HIGH`) se publica siempre en la pestaña **Security** del repo; solo una
vulnerabilidad `CRITICAL` hace fallar el job.

Si una `CRITICAL` no tiene fix disponible todavía y hay que aceptar el riesgo
de forma consciente, se ignora explícitamente añadiendo su CVE a un
`.trivyignore` en la raíz del repo (no existe ninguno hoy).

## Problemas frecuentes

`network parking-shared ... not found` → te falta el primer comando.

El puerto 5434 ya está en uso → cámbialo en el `.env`. El puerto del host es configurable; el interno no.

Cambias el `.env` y no se entera → `docker compose up -d --force-recreate`. Si tocas usuario o contraseña, además `docker compose down -v`: esas credenciales solo se aplican al crear la BD por primera vez.