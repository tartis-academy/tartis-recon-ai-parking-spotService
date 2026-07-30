

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