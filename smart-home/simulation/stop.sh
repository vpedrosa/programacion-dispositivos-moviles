#!/usr/bin/env bash
# stop.sh — Para todos los procesos de simulación de forma limpia
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$SCRIPT_DIR/logs"

echo "========================================"
echo " Parando simulación Smart Home"
echo "========================================"
echo ""

STOPPED=0

# Parar todos los procesos con PID guardado
for pid_file in "$LOG_DIR"/*.pid; do
    [ -f "$pid_file" ] || continue

    PID=$(cat "$pid_file")
    NAME=$(basename "$pid_file" .pid)

    if kill -0 "$PID" 2>/dev/null; then
        echo "  Parando $NAME (PID: $PID)..."
        kill "$PID" 2>/dev/null || true
        STOPPED=$((STOPPED + 1))
    else
        echo "  $NAME (PID: $PID) ya no está activo"
    fi

    rm -f "$pid_file"
done

# Esperar a que terminen
sleep 1

# Verificar que no queda ninguno
for pid_file in "$LOG_DIR"/*.pid; do
    [ -f "$pid_file" ] || continue
    PID=$(cat "$pid_file")
    if kill -0 "$PID" 2>/dev/null; then
        echo "  Forzando parada de PID $PID..."
        kill -9 "$PID" 2>/dev/null || true
    fi
    rm -f "$pid_file"
done

echo ""
echo "Procesos detenidos: $STOPPED"
echo "Logs disponibles en: $LOG_DIR/"
echo "========================================"
