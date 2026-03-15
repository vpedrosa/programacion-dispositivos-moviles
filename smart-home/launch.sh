#!/usr/bin/env bash
# launch.sh — Arranca la simulación Matter + dashboard web
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SIM_DIR="$ROOT_DIR/simulation"
DASH_DIR="$SIM_DIR/dashboard"

cleanup() {
    echo ""
    echo "Parando servicios..."
    [ -n "${SIM_PID:-}" ] && kill "$SIM_PID" 2>/dev/null
    [ -n "${DASH_PID:-}" ] && kill "$DASH_PID" 2>/dev/null
    wait 2>/dev/null
    echo "Listo."
}
trap cleanup EXIT INT TERM

# Instalar dependencias si faltan
if [ ! -d "$SIM_DIR/node_modules" ]; then
    echo "--- Instalando dependencias de simulación ---"
    (cd "$SIM_DIR" && npm install)
    echo ""
fi

if [ ! -d "$DASH_DIR/node_modules" ]; then
    echo "--- Instalando dependencias del dashboard ---"
    (cd "$DASH_DIR" && npm install)
    echo ""
fi

export MATTER_LOG_LEVEL="${MATTER_LOG_LEVEL:-notice}"

echo "--- Arrancando simulación Matter (puerto 5540-5566, WS 8085) ---"
(cd "$SIM_DIR" && node src/main.mjs) &
SIM_PID=$!

echo "--- Arrancando dashboard web (puerto 3000) ---"
(cd "$DASH_DIR" && npm run dev -- --port 3000) &
DASH_PID=$!

echo ""
echo "  Simulación Matter : PID $SIM_PID"
echo "  Dashboard web     : http://localhost:3000 (PID $DASH_PID)"
echo ""
echo "  Ctrl+C para parar ambos."
echo ""

wait
