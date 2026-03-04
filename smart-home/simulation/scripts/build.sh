#!/usr/bin/env bash
# build.sh — Compila los ejemplos de dispositivos Matter y chip-tool para Linux x64
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SIMULATION_DIR="$(dirname "$SCRIPT_DIR")"
CHIP_DIR="$SIMULATION_DIR/third_party/connectedhomeip"
OUT_DIR="$SIMULATION_DIR/devices/bin"

mkdir -p "$OUT_DIR"

# Activar entorno si no está activo
if [ -z "${CHIP_ROOT:-}" ]; then
    echo "--- Activando entorno connectedhomeip ---"
    cd "$CHIP_DIR"
    source scripts/activate.sh
fi

cd "$CHIP_DIR"

# Dispositivos a compilar y su ruta en el SDK
declare -A DEVICES=(
    ["chip-tool"]="examples/chip-tool"
    ["lighting-app"]="examples/lighting-app/linux"
    ["lock-app"]="examples/lock-app/linux"
    ["all-clusters-app"]="examples/all-clusters-app/linux"
    ["thermostat"]="examples/thermostat/linux"
    ["window-app"]="examples/window-app/linux"
)

# Si se pasan argumentos, compilar solo esos
if [ $# -gt 0 ]; then
    TARGETS=("$@")
else
    TARGETS=("${!DEVICES[@]}")
fi

for target in "${TARGETS[@]}"; do
    src="${DEVICES[$target]:-}"
    if [ -z "$src" ]; then
        echo "WARN: Dispositivo desconocido '$target', saltando."
        continue
    fi

    echo ""
    echo "=== Compilando $target ==="
    build_dir="out/$target"

    # Generar proyecto Ninja
    gn gen "$build_dir" --root="$src" --args='chip_config_network_layer_ble=false'

    # Compilar
    ninja -C "$build_dir"

    # Copiar binario al directorio de salida
    binary=$(find "$build_dir" -maxdepth 1 -type f -executable | head -1)
    if [ -n "$binary" ]; then
        cp "$binary" "$OUT_DIR/$(basename "$binary")"
        echo "OK: $target -> $OUT_DIR/$(basename "$binary")"
    else
        echo "WARN: No se encontró binario para $target"
    fi
done

echo ""
echo "=== Compilación completa ==="
echo "Binarios disponibles en: $OUT_DIR"
ls -la "$OUT_DIR" 2>/dev/null || true
