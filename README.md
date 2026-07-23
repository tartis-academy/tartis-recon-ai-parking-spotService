

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

## Problemas frecuentes

`network parking-shared ... not found` → te falta el primer comando.

El puerto 5434 ya está en uso → cámbialo en el `.env`. El puerto del host es configurable; el interno no.

Cambias el `.env` y no se entera → `docker compose up -d --force-recreate`. Si tocas usuario o contraseña, además `docker compose down -v`: esas credenciales solo se aplican al crear la BD por primera vez.