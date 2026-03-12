#!/usr/bin/env bash
# start.sh — Arranca los 27 dispositivos Matter virtuales (matter.js)
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$SCRIPT_DIR/logs"

mkdir -p "$LOG_DIR"

# Instalar dependencias si no existen
if [ ! -d "$SCRIPT_DIR/node_modules" ]; then
    echo "--- Instalando dependencias (npm install) ---"
    cd "$SCRIPT_DIR"
    npm install
    echo ""
fi

export MATTER_LOG_LEVEL="${MATTER_LOG_LEVEL:-notice}"

echo "--- Arrancando simulación Matter (log: $MATTER_LOG_LEVEL) ---"
cd "$SCRIPT_DIR"

if [ "${1:-}" = "--background" ] || [ "${1:-}" = "-b" ]; then
    node src/main.mjs > "$LOG_DIR/matter.log" 2>&1 &
    MATTER_PID=$!
    echo "$MATTER_PID" > "$LOG_DIR/matter.pid"

    sleep 3

    if kill -0 "$MATTER_PID" 2>/dev/null; then
        echo "Simulación arrancada (PID: $MATTER_PID)"
        echo "Log: $LOG_DIR/matter.log"
        echo "Para parar: ./stop.sh"
    else
        echo "ERROR: La simulación no arrancó. Ver log:"
        tail -20 "$LOG_DIR/matter.log"
        exit 1
    fi
else
    echo "Ejecutando en primer plano (Ctrl+C para parar)"
    echo ""
    exec node src/main.mjs
fi
