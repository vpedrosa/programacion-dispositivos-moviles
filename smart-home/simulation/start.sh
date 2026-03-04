#!/usr/bin/env bash
# start.sh — Orquestador: arranca dispositivos simulados, backend y UI web
# Uso:
#   ./start.sh                          # Arranca todos los dispositivos
#   ./start.sh --devices lighting,lock  # Solo bombilla y cerradura
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCRIPTS="$SCRIPT_DIR/scripts"
LOG_DIR="$SCRIPT_DIR/logs"
BIN_DIR="$SCRIPT_DIR/devices/bin"

mkdir -p "$LOG_DIR"

# Configuración de dispositivos: tipo → (node-id, puerto)
declare -A DEVICE_NODES=(
    ["lighting"]=1
    ["lock"]=2
    ["smoke"]=3
    ["temperature"]=4
    ["thermostat"]=5
    ["window"]=6
)

declare -A DEVICE_PORTS=(
    ["lighting"]=5540
    ["lock"]=5541
    ["smoke"]=5542
    ["temperature"]=5543
    ["thermostat"]=5544
    ["window"]=5545
)

declare -A DEVICE_NAMES=(
    ["lighting"]="Bombilla Salón"
    ["lock"]="Cerradura Entrada"
    ["smoke"]="Sensor de Humo"
    ["temperature"]="Sensor Temperatura"
    ["thermostat"]="Termostato"
    ["window"]="Persiana Salón"
)

# Parse argumentos
ALL_TYPES=("lighting" "lock" "smoke" "temperature" "thermostat" "window")
SELECTED_TYPES=("${ALL_TYPES[@]}")

while [[ $# -gt 0 ]]; do
    case "$1" in
        --devices)
            IFS=',' read -ra SELECTED_TYPES <<< "$2"
            shift 2
            ;;
        --help|-h)
            echo "Uso: $0 [--devices tipo1,tipo2,...]"
            echo "Tipos: lighting, lock, smoke, temperature, thermostat, window"
            exit 0
            ;;
        *)
            echo "Argumento desconocido: $1"
            exit 1
            ;;
    esac
done

echo "========================================"
echo " Smart Home — Simulación Matter"
echo "========================================"
echo ""

# 1. Verificar que los binarios existen
MISSING=()
for dtype in "${SELECTED_TYPES[@]}"; do
    # Verificar que el tipo existe
    if [ -z "${DEVICE_NODES[$dtype]:-}" ]; then
        echo "WARN: Tipo desconocido '$dtype', saltando."
        continue
    fi
done

# 2. Arrancar dispositivos
echo "--- Arrancando dispositivos ---"
LAUNCHED_PIDS=()

for dtype in "${SELECTED_TYPES[@]}"; do
    node_id="${DEVICE_NODES[$dtype]:-}"
    port="${DEVICE_PORTS[$dtype]:-}"

    if [ -z "$node_id" ]; then continue; fi

    echo "  Lanzando $dtype (nodo: $node_id, puerto: $port)..."
    "$SCRIPTS/run-device.sh" "$dtype" "$port" 2>/dev/null || {
        echo "  WARN: No se pudo lanzar $dtype (¿binario compilado?)"
        continue
    }

    PID_FILE="$LOG_DIR/${dtype}_${port}.pid"
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        LAUNCHED_PIDS+=("$PID")
        echo "  OK: $dtype arrancado (PID: $PID)"
    fi
done

echo ""

# 3. Esperar a que los dispositivos estén listos
echo "--- Esperando a que los dispositivos estén listos (3s) ---"
sleep 3

# 4. Verificar salud de cada dispositivo
echo "--- Verificando dispositivos ---"
HEALTHY=0
TOTAL=0

for dtype in "${SELECTED_TYPES[@]}"; do
    port="${DEVICE_PORTS[$dtype]:-}"
    if [ -z "$port" ]; then continue; fi

    PID_FILE="$LOG_DIR/${dtype}_${port}.pid"
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        TOTAL=$((TOTAL + 1))
        if kill -0 "$PID" 2>/dev/null; then
            HEALTHY=$((HEALTHY + 1))
            echo "  ✓ $dtype (PID: $PID) — activo"
        else
            echo "  ✗ $dtype (PID: $PID) — NO RESPONDE"
        fi
    fi
done

echo ""
echo "Dispositivos activos: $HEALTHY/$TOTAL"
echo ""

# 5. Comisionar dispositivos
echo "--- Comisionando dispositivos ---"
for dtype in "${SELECTED_TYPES[@]}"; do
    node_id="${DEVICE_NODES[$dtype]:-}"
    if [ -z "$node_id" ]; then continue; fi

    port="${DEVICE_PORTS[$dtype]:-}"
    PID_FILE="$LOG_DIR/${dtype}_${port}.pid"
    if [ ! -f "$PID_FILE" ]; then continue; fi

    echo "  Comisionando $dtype (nodo: $node_id)..."
    "$SCRIPTS/commission.sh" "$node_id" 20202021 > "$LOG_DIR/commission_${dtype}.log" 2>&1 || {
        echo "  WARN: Comisionamiento de $dtype puede haber fallado (ver logs)"
    }
done

echo ""

# 6. Arrancar backend web
echo "--- Arrancando backend web ---"
"$SCRIPTS/run-web.sh" 0.0.0.0 8080 > "$LOG_DIR/backend.log" 2>&1 &
BACKEND_PID=$!
echo "$BACKEND_PID" > "$LOG_DIR/backend.pid"
echo "  Backend arrancado (PID: $BACKEND_PID)"

# 7. Registrar dispositivos en el backend
sleep 2
echo ""
echo "--- Registrando dispositivos en el backend ---"
for dtype in "${SELECTED_TYPES[@]}"; do
    node_id="${DEVICE_NODES[$dtype]:-}"
    port="${DEVICE_PORTS[$dtype]:-}"
    name="${DEVICE_NAMES[$dtype]:-$dtype}"
    if [ -z "$node_id" ]; then continue; fi

    curl -s -X POST "http://localhost:8080/api/devices/register?node_id=$node_id&device_type=$dtype&name=$(python3 -c "import urllib.parse; print(urllib.parse.quote('$name'))")&port=$port" > /dev/null 2>&1 || true
    echo "  Registrado: $name (nodo $node_id)"
done

echo ""
echo "========================================"
echo " Simulación arrancada"
echo " UI web: http://localhost:8080"
echo " API:    http://localhost:8080/docs"
echo " Logs:   $LOG_DIR/"
echo " Para parar: ./stop.sh"
echo "========================================"
