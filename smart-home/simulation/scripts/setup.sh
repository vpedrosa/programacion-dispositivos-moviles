#!/usr/bin/env bash
# setup.sh — Instala dependencias del sistema y activa el entorno de build de connectedhomeip
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SIMULATION_DIR="$(dirname "$SCRIPT_DIR")"
CHIP_DIR="$SIMULATION_DIR/third_party/connectedhomeip"

echo "=== Configuración del entorno connectedhomeip ==="

# 1. Verificar que el submódulo existe
if [ ! -d "$CHIP_DIR" ]; then
    echo "ERROR: No se encontró connectedhomeip en $CHIP_DIR"
    echo "Ejecuta: git submodule update --init smart-home/simulation/third_party/connectedhomeip"
    exit 1
fi

# 2. Instalar dependencias del sistema (Debian/Ubuntu)
echo ""
echo "--- Instalando dependencias del sistema ---"
sudo apt-get update -qq
sudo apt-get install -y --no-install-recommends \
    git \
    gcc \
    g++ \
    pkg-config \
    libssl-dev \
    libdbus-1-dev \
    libglib2.0-dev \
    libavahi-client-dev \
    ninja-build \
    python3-venv \
    python3-dev \
    python3-pip \
    unzip \
    libgirepository1.0-dev \
    libcairo2-dev \
    libreadline-dev \
    generate-ninja

# 3. Inicializar submódulos internos de connectedhomeip
echo ""
echo "--- Inicializando submódulos de connectedhomeip ---"
cd "$CHIP_DIR"
git submodule update --init --depth 1

# 4. Activar entorno de build
echo ""
echo "--- Activando entorno de build ---"
source scripts/activate.sh

echo ""
echo "=== Entorno configurado correctamente ==="
echo "Para compilar, ejecuta: ./scripts/build.sh"
