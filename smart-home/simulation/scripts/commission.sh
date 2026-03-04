#!/usr/bin/env bash
# commission.sh — Comisiona un dispositivo Matter con chip-tool
# Uso: ./commission.sh <node-id> [passcode]
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SIMULATION_DIR="$(dirname "$SCRIPT_DIR")"
BIN_DIR="$SIMULATION_DIR/devices/bin"
CHIP_TOOL="$BIN_DIR/chip-tool"

NODE_ID="${1:-}"
PASSCODE="${2:-20202021}"

if [ -z "$NODE_ID" ]; then
    echo "Uso: $0 <node-id> [passcode]"
    echo "  node-id:  Identificador numérico del nodo (ej: 1, 2, 3)"
    echo "  passcode: Código de comisionamiento (por defecto: 20202021)"
    exit 1
fi

if [ ! -x "$CHIP_TOOL" ]; then
    echo "ERROR: chip-tool no encontrado en $CHIP_TOOL"
    echo "Ejecuta primero: ./scripts/build.sh chip-tool"
    exit 1
fi

echo "=== Comisionando dispositivo (nodo: $NODE_ID, passcode: $PASSCODE) ==="
"$CHIP_TOOL" pairing onnetwork "$NODE_ID" "$PASSCODE" 2>&1 | tee /dev/stderr | \
    python3 -c "
import sys, json
output = sys.stdin.read()
success = 'Commissioner: Commissioning complete' in output or 'Successfully finished commissioning' in output
print(json.dumps({'node_id': $NODE_ID, 'success': success, 'passcode': $PASSCODE}))
" 2>/dev/null || echo "{\"node_id\": $NODE_ID, \"success\": false, \"error\": \"commissioning failed\"}"
