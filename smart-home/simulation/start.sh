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

echo "--- Arrancando simulación Matter ---"
cd "$SCRIPT_DIR"
node src/main.mjs > "$LOG_DIR/matter.log" 2>&1 &
MATTER_PID=$!
echo "$MATTER_PID" > "$LOG_DIR/matter.pid"

# Esperar a que arranque
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
