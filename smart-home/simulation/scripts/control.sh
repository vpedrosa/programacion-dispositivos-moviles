#!/usr/bin/env bash
# control.sh — Envía comandos y lee atributos de dispositivos Matter via chip-tool
# Uso:
#   ./control.sh lighting on <node-id> [endpoint]
#   ./control.sh lighting off <node-id> [endpoint]
#   ./control.sh lighting brightness <node-id> <level> [endpoint]
#   ./control.sh lighting color <node-id> <hue> <saturation> [endpoint]
#   ./control.sh lock lock <node-id> [endpoint]
#   ./control.sh lock unlock <node-id> [endpoint]
#   ./control.sh lock status <node-id> [endpoint]
#   ./control.sh thermostat read <node-id> [endpoint]
#   ./control.sh thermostat set <node-id> <temperature_x100> [endpoint]
#   ./control.sh window set <node-id> <percent> [endpoint]
#   ./control.sh window status <node-id> [endpoint]
#   ./control.sh sensor temperature <node-id> [endpoint]
#   ./control.sh sensor smoke <node-id> [endpoint]
#   ./control.sh trigger smoke-alarm <node-id> [endpoint]
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SIMULATION_DIR="$(dirname "$SCRIPT_DIR")"
BIN_DIR="$SIMULATION_DIR/devices/bin"
CHIP_TOOL="$BIN_DIR/chip-tool"

if [ ! -x "$CHIP_TOOL" ]; then
    echo '{"error": "chip-tool not found. Run ./scripts/build.sh chip-tool"}'
    exit 1
fi

DEVICE="${1:-}"
ACTION="${2:-}"
NODE_ID="${3:-}"

if [ -z "$DEVICE" ] || [ -z "$ACTION" ] || [ -z "$NODE_ID" ]; then
    echo "Uso: $0 <device> <action> <node-id> [args...]"
    echo "Dispositivos: lighting, lock, thermostat, window, sensor, trigger"
    exit 1
fi

ENDPOINT="${4:-1}"

# Función para formatear salida como JSON
json_result() {
    local status="$1"
    local message="$2"
    echo "{\"device\": \"$DEVICE\", \"action\": \"$ACTION\", \"node_id\": $NODE_ID, \"endpoint\": $ENDPOINT, \"status\": \"$status\", \"message\": \"$message\"}"
}

case "$DEVICE" in
    lighting)
        case "$ACTION" in
            on)
                "$CHIP_TOOL" onoff on "$NODE_ID" "$ENDPOINT" 2>/dev/null && \
                    json_result "ok" "light turned on" || json_result "error" "command failed"
                ;;
            off)
                "$CHIP_TOOL" onoff off "$NODE_ID" "$ENDPOINT" 2>/dev/null && \
                    json_result "ok" "light turned off" || json_result "error" "command failed"
                ;;
            brightness)
                LEVEL="${4:?Falta nivel (0-254)}"
                ENDPOINT="${5:-1}"
                "$CHIP_TOOL" levelcontrol move-to-level "$LEVEL" 0 0 0 "$NODE_ID" "$ENDPOINT" 2>/dev/null && \
                    json_result "ok" "brightness set to $LEVEL" || json_result "error" "command failed"
                ;;
            color)
                HUE="${4:?Falta hue (0-254)}"
                SAT="${5:?Falta saturation (0-254)}"
                ENDPOINT="${6:-1}"
                "$CHIP_TOOL" colorcontrol move-to-hue-and-saturation "$HUE" "$SAT" 0 0 0 "$NODE_ID" "$ENDPOINT" 2>/dev/null && \
                    json_result "ok" "color set to hue=$HUE sat=$SAT" || json_result "error" "command failed"
                ;;
            status)
                "$CHIP_TOOL" onoff read on-off "$NODE_ID" "$ENDPOINT" 2>/dev/null | \
                    grep -oP 'value: \K.*' | head -1 || echo '{"error": "read failed"}'
                ;;
            *)
                echo "{\"error\": \"unknown lighting action: $ACTION\"}"
                ;;
        esac
        ;;
    lock)
        case "$ACTION" in
            lock)
                "$CHIP_TOOL" doorlock lock-door "$NODE_ID" "$ENDPOINT" --timedInteractionTimeoutMs 1000 2>/dev/null && \
                    json_result "ok" "door locked" || json_result "error" "command failed"
                ;;
            unlock)
                "$CHIP_TOOL" doorlock unlock-door "$NODE_ID" "$ENDPOINT" --timedInteractionTimeoutMs 1000 2>/dev/null && \
                    json_result "ok" "door unlocked" || json_result "error" "command failed"
                ;;
            status)
                "$CHIP_TOOL" doorlock read lock-state "$NODE_ID" "$ENDPOINT" 2>/dev/null | \
                    grep -oP 'value: \K.*' | head -1 || echo '{"error": "read failed"}'
                ;;
            *)
                echo "{\"error\": \"unknown lock action: $ACTION\"}"
                ;;
        esac
        ;;
    thermostat)
        case "$ACTION" in
            read)
                echo "{"
                echo "  \"local_temperature\": $(
                    "$CHIP_TOOL" thermostat read local-temperature "$NODE_ID" "$ENDPOINT" 2>/dev/null | \
                    grep -oP 'value: \K.*' | head -1 || echo "null"
                ),"
                echo "  \"occupied_heating_setpoint\": $(
                    "$CHIP_TOOL" thermostat read occupied-heating-setpoint "$NODE_ID" "$ENDPOINT" 2>/dev/null | \
                    grep -oP 'value: \K.*' | head -1 || echo "null"
                )"
                echo "}"
                ;;
            set)
                TEMP="${4:?Falta temperatura (x100, ej: 2200 = 22.00°C)}"
                ENDPOINT="${5:-1}"
                "$CHIP_TOOL" thermostat write occupied-heating-setpoint "$TEMP" "$NODE_ID" "$ENDPOINT" 2>/dev/null && \
                    json_result "ok" "setpoint set to $TEMP" || json_result "error" "command failed"
                ;;
            *)
                echo "{\"error\": \"unknown thermostat action: $ACTION\"}"
                ;;
        esac
        ;;
    window)
        case "$ACTION" in
            set)
                PERCENT="${4:?Falta porcentaje (0-100)}"
                ENDPOINT="${5:-1}"
                "$CHIP_TOOL" windowcovering go-to-lift-percentage "$PERCENT" "$NODE_ID" "$ENDPOINT" 2>/dev/null && \
                    json_result "ok" "window set to $PERCENT%" || json_result "error" "command failed"
                ;;
            status)
                "$CHIP_TOOL" windowcovering read current-position-lift-percentage "$NODE_ID" "$ENDPOINT" 2>/dev/null | \
                    grep -oP 'value: \K.*' | head -1 || echo '{"error": "read failed"}'
                ;;
            *)
                echo "{\"error\": \"unknown window action: $ACTION\"}"
                ;;
        esac
        ;;
    sensor)
        case "$ACTION" in
            temperature)
                "$CHIP_TOOL" temperaturemeasurement read measured-value "$NODE_ID" "$ENDPOINT" 2>/dev/null | \
                    grep -oP 'value: \K.*' | head -1 || echo '{"error": "read failed"}'
                ;;
            smoke)
                "$CHIP_TOOL" smokecoalarm read smoke-state "$NODE_ID" "$ENDPOINT" 2>/dev/null | \
                    grep -oP 'value: \K.*' | head -1 || echo '{"error": "read failed"}'
                ;;
            *)
                echo "{\"error\": \"unknown sensor action: $ACTION\"}"
                ;;
        esac
        ;;
    trigger)
        case "$ACTION" in
            smoke-alarm)
                # Dispara alarma de humo via named pipe si está disponible
                PIPE_DIR="$SIMULATION_DIR/devices/pipes"
                PIPE="$PIPE_DIR/smoke_${NODE_ID}"
                if [ -p "$PIPE" ]; then
                    echo '{"Name":"SmokeAlarm","NewState":1}' > "$PIPE"
                    json_result "ok" "smoke alarm triggered via pipe"
                else
                    # Alternativa: usar cluster command si está disponible
                    "$CHIP_TOOL" smokecoalarm self-test-request "$NODE_ID" "$ENDPOINT" 2>/dev/null && \
                        json_result "ok" "smoke alarm self-test triggered" || \
                        json_result "error" "no pipe available and self-test failed"
                fi
                ;;
            water-leak)
                PIPE_DIR="$SIMULATION_DIR/devices/pipes"
                PIPE="$PIPE_DIR/water_${NODE_ID}"
                if [ -p "$PIPE" ]; then
                    echo '{"Name":"WaterLeak","NewState":1}' > "$PIPE"
                    json_result "ok" "water leak triggered via pipe"
                else
                    json_result "error" "no pipe available for water leak trigger"
                fi
                ;;
            *)
                echo "{\"error\": \"unknown trigger: $ACTION\"}"
                ;;
        esac
        ;;
    *)
        echo "{\"error\": \"unknown device type: $DEVICE\"}"
        ;;
esac
