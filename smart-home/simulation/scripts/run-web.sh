#!/usr/bin/env bash
# run-web.sh — Arranca el backend API REST para control de dispositivos
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SIMULATION_DIR="$(dirname "$SCRIPT_DIR")"
BACKEND_DIR="$SIMULATION_DIR/web/backend"
LOG_DIR="$SIMULATION_DIR/logs"

mkdir -p "$LOG_DIR"

HOST="${1:-0.0.0.0}"
PORT="${2:-8080}"

# Crear venv si no existe
if [ ! -d "$BACKEND_DIR/.venv" ]; then
    echo "--- Creando entorno virtual ---"
    python3 -m venv "$BACKEND_DIR/.venv"
fi

# Activar venv e instalar dependencias
source "$BACKEND_DIR/.venv/bin/activate"
pip install -q -r "$BACKEND_DIR/requirements.txt"

echo "=== Arrancando backend en http://$HOST:$PORT ==="
echo "  Documentación: http://$HOST:$PORT/docs"
echo "  Log: $LOG_DIR/backend.log"
echo ""

uvicorn main:app \
    --host "$HOST" \
    --port "$PORT" \
    --app-dir "$BACKEND_DIR" \
    --reload \
    2>&1 | tee "$LOG_DIR/backend.log"
