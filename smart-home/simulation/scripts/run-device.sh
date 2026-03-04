#!/usr/bin/env bash
# run-device.sh — Lanza un dispositivo Matter simulado
# Uso: ./run-device.sh <tipo> [puerto]
# Tipos: lighting, lock, smoke, temperature, thermostat, window, all-clusters
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SIMULATION_DIR="$(dirname "$SCRIPT_DIR")"
BIN_DIR="$SIMULATION_DIR/devices/bin"
LOG_DIR="$SIMULATION_DIR/logs"

DEVICE_TYPE="${1:-}"
PORT="${2:-5540}"

if [ -z "$DEVICE_TYPE" ]; then
    echo "Uso: $0 <tipo> [puerto]"
    echo "Tipos disponibles: lighting, lock, smoke, temperature, thermostat, window, all-clusters"
    exit 1
fi

# Mapeo de tipo a binario
declare -A DEVICE_MAP=(
    ["lighting"]="lighting-app"
    ["lock"]="lock-app"
    ["smoke"]="all-clusters-app"
    ["temperature"]="all-clusters-app"
    ["thermostat"]="thermostat-app"
    ["window"]="chip-window-app"
    ["all-clusters"]="all-clusters-app"
)

BINARY_NAME="${DEVICE_MAP[$DEVICE_TYPE]:-}"
if [ -z "$BINARY_NAME" ]; then
    echo "ERROR: Tipo de dispositivo desconocido '$DEVICE_TYPE'"
    echo "Tipos disponibles: ${!DEVICE_MAP[*]}"
    exit 1
fi

BINARY_PATH="$BIN_DIR/$BINARY_NAME"
if [ ! -x "$BINARY_PATH" ]; then
    echo "ERROR: Binario no encontrado en $BINARY_PATH"
    echo "Ejecuta primero: ./scripts/build.sh $BINARY_NAME"
    exit 1
fi

# Directorio de datos único por instancia (evita conflictos entre dispositivos del mismo tipo)
KVS_DIR="$SIMULATION_DIR/devices/kvs"
mkdir -p "$KVS_DIR"
KVS_FILE="$KVS_DIR/${DEVICE_TYPE}_${PORT}.kvs"

# Directorio de logs
mkdir -p "$LOG_DIR"
LOG_FILE="$LOG_DIR/${DEVICE_TYPE}_${PORT}.log"

echo "=== Lanzando dispositivo: $DEVICE_TYPE ==="
echo "  Binario: $BINARY_PATH"
echo "  Puerto:  $PORT"
echo "  KVS:     $KVS_FILE"
echo "  Log:     $LOG_FILE"
echo ""

# Argumentos comunes
ARGS=(
    --secured-device-port "$PORT"
    --KVS "$KVS_FILE"
    --discriminator "$((1000 + PORT - 5540))"
)

# Argumentos específicos por tipo
case "$DEVICE_TYPE" in
    lock)
        # lock-app soporta named pipes para control externo
        PIPE_DIR="$SIMULATION_DIR/devices/pipes"
        mkdir -p "$PIPE_DIR"
        ARGS+=(--app-pipe "$PIPE_DIR/lock_${PORT}")
        ;;
esac

echo "Ejecutando: $BINARY_PATH ${ARGS[*]}"
echo "PID guardado en: $LOG_DIR/${DEVICE_TYPE}_${PORT}.pid"
echo ""

# Lanzar en background, guardar PID
"$BINARY_PATH" "${ARGS[@]}" > "$LOG_FILE" 2>&1 &
PID=$!
echo "$PID" > "$LOG_DIR/${DEVICE_TYPE}_${PORT}.pid"

echo "Dispositivo $DEVICE_TYPE arrancado (PID: $PID)"
echo "Usa 'tail -f $LOG_FILE' para ver los logs"
echo "Passcode por defecto: 20202021"
