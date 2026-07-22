#!/usr/bin/env bash
#
# Levanta el PostgreSQL DEDICADO de spot-service (perfil 'prod', o para
# levantar este servicio aislado).
#
# Para el dia a dia en DEV usa el Postgres UNICO compartido (5 schemas) del
# repo de infra: (cd ../tartis-recon-ia-parking-infra && ./setup.sh)
# Ese trae tambien pgAdmin y SonarQube. Este script de aqui NO es ese flujo.
#
#   ./setup.sh          levanta la BD dedicada
#   ./setup.sh down     para el contenedor (mantiene los datos)
#   ./setup.sh clean    para el contenedor y BORRA los datos
#
set -euo pipefail

# Ejecutarse siempre desde la raiz del repo, se lance desde donde se lance.
cd "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

NETWORK="parking-shared"
CONTAINER="parking-spot-postgres"

if [ -t 1 ]; then
  RED=$'\e[31m'; GREEN=$'\e[32m'; YELLOW=$'\e[33m'; BOLD=$'\e[1m'; OFF=$'\e[0m'
else
  RED=""; GREEN=""; YELLOW=""; BOLD=""; OFF=""
fi

info() { echo "${BOLD}==>${OFF} $1"; }
ok()   { echo "  ${GREEN}OK${OFF}   $1"; }
warn() { echo "  ${YELLOW}!${OFF}    $1"; }
die()  { echo "  ${RED}ERROR${OFF} $1" >&2; exit 1; }

# --- Comprobaciones previas -------------------------------------------------

command -v docker >/dev/null 2>&1 || die "Docker no esta instalado o no esta en el PATH."

docker compose version >/dev/null 2>&1 \
  || die "Necesitas Docker Compose v2 ('docker compose', no 'docker-compose')."

docker info >/dev/null 2>&1 \
  || die "El demonio de Docker no responde. Arranca Docker Desktop y reintenta."

# --- Parar / limpiar --------------------------------------------------------

case "${1:-up}" in
  down|clean)
    FLAG=""
    [ "${1}" = "clean" ] && FLAG="-v"
    info "Parando spot-service"
    docker compose down $FLAG || true
    [ "${1}" = "clean" ] && warn "Datos de la BD borrados."
    ok "Hecho."
    exit 0
    ;;
  up) ;;
  *) die "Opcion desconocida: '$1'. Usa: up | down | clean" ;;
esac

# --- 1. Red compartida ------------------------------------------------------

info "Red compartida '$NETWORK'"
if docker network inspect "$NETWORK" >/dev/null 2>&1; then
  ok "Ya existe."
else
  docker network create "$NETWORK" >/dev/null
  ok "Creada."
fi

# --- 2. Fichero .env --------------------------------------------------------

info "Fichero .env"
if [ -f .env ]; then
  ok ".env ya existe, no lo toco."
elif [ -f .env.example ]; then
  cp .env.example .env
  ok ".env creado desde .env.example."
  warn "Lleva contrasenas 'change.me'. Cambialas si esto no es tu portatil."
else
  die "Falta .env.example"
fi

# --- 3. Levantar la BD ------------------------------------------------------

info "PostgreSQL de spot-service"
docker compose up -d

# --- 4. Esperar a que este sana ---------------------------------------------

info "Esperando a que este healthy"
FAILED=0
waited=0
printf "  ...  "
while [ "$waited" -lt 60 ]; do
  state="$(docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}nohealth{{end}}' "$CONTAINER" 2>/dev/null || echo "missing")"
  case "$state" in
    healthy) echo; ok "$CONTAINER healthy (${waited}s)"; break ;;
    missing) echo; warn "$CONTAINER no existe."; FAILED=1; break ;;
  esac
  printf "."
  sleep 2; waited=$((waited + 2))
done
if [ "$waited" -ge 60 ]; then
  echo; warn "Sigue sin estar healthy tras 60s. Mira: docker logs $CONTAINER"
  FAILED=1
fi

# --- 5. Resumen -------------------------------------------------------------

echo
if [ "$FAILED" -eq 0 ]; then
  info "${GREEN}spot-service listo${OFF}"
else
  info "${YELLOW}Levantado con avisos${OFF} (revisa los mensajes de arriba)"
fi

cat <<EOF

  BD spot-service
    desde tu maquina    localhost:${SPOT_DB_PORT:-5434}
    desde pgAdmin       parking-spot-postgres:5432

  pgAdmin y SonarQube se levantan desde el repo de vehicle-service.

  Parar:   ./setup.sh down       Parar y borrar datos:  ./setup.sh clean
EOF

exit "$FAILED"
