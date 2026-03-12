#!/usr/bin/env bash
# start.sh — Arranca la simulación mock de dispositivos Smart Home
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$SCRIPT_DIR/logs"

mkdir -p "$LOG_DIR"

echo "========================================"
echo " Smart Home — Simulación Mock"
echo "========================================"
echo ""
echo "Los dispositivos se registran automáticamente al arrancar el backend."
echo ""

# Arrancar backend (incluye los 6 dispositivos pre-registrados)
echo "--- Arrancando backend web ---"
"$SCRIPT_DIR/scripts/run-web.sh" 0.0.0.0 8080 > "$LOG_DIR/backend.log" 2>&1 &
BACKEND_PID=$!
echo "$BACKEND_PID" > "$LOG_DIR/backend.pid"

# Esperar a que el backend esté listo
echo "  Esperando a que el backend esté listo..."
for i in $(seq 1 10); do
    if curl -s http://localhost:8080/api/devices > /dev/null 2>&1; then
        break
    fi
    sleep 1
done

echo ""
echo "========================================"
echo " Simulación arrancada"
echo " UI web: http://localhost:8080"
echo " API:    http://localhost:8080/docs"
echo " Logs:   $LOG_DIR/"
echo " Para parar: ./stop.sh"
echo "========================================"
