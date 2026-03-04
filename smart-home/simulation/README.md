# Simulación Matter (connectedhomeip)

Módulo de simulación de dispositivos Matter reales usando el SDK [connectedhomeip](https://github.com/project-chip/connectedhomeip) de la CSA (Connectivity Standards Alliance).

## Estructura

```
simulation/
├── scripts/           # Scripts de setup, build y control
│   ├── setup.sh       # Instala dependencias y activa el entorno
│   ├── build.sh       # Compila dispositivos y chip-tool
│   ├── run-device.sh  # Lanza un dispositivo simulado
│   ├── commission.sh  # Comisiona un dispositivo con chip-tool
│   └── run-web.sh     # Arranca el backend de la UI web
├── devices/
│   └── bin/           # Binarios compilados
├── web/
│   ├── backend/       # API REST (Python FastAPI)
│   └── frontend/      # UI web para control de dispositivos
├── logs/              # Logs de los procesos
├── start.sh           # Orquestador: arranca todo
├── stop.sh            # Orquestador: para todo
└── third_party/
    └── connectedhomeip/  # SDK Matter (submódulo git)
```

## Dispositivos simulados

| Dispositivo | Ejemplo del SDK | Cluster Matter |
|---|---|---|
| Bombilla | `lighting-app` | OnOff, LevelControl, ColorControl |
| Cerradura | `lock-app` | DoorLock |
| Sensores (humo, agua, temp) | `all-clusters-app` | SmokeCoAlarm, TemperatureMeasurement |
| Termostato | `thermostat` | Thermostat |
| Persianas | `window-app` | WindowCovering |

## Setup

### 1. Inicializar submódulo

```bash
git submodule update --init smart-home/simulation/third_party/connectedhomeip
```

### 2. Instalar dependencias y activar entorno

```bash
cd smart-home/simulation
./scripts/setup.sh
```

Esto instala las dependencias del sistema (libssl-dev, libdbus-1-dev, ninja-build, etc.) y activa el entorno de build de connectedhomeip.

### 3. Compilar dispositivos y chip-tool

```bash
# Compilar todo
./scripts/build.sh

# Compilar solo algunos
./scripts/build.sh chip-tool lighting-app lock-app
```

Los binarios se generan en `devices/bin/`.

### 4. Comisionar un dispositivo

```bash
# Lanzar dispositivo (ej: bombilla en puerto 5540)
./scripts/run-device.sh lighting 5540

# En otra terminal, comisionar con chip-tool
./scripts/commission.sh 1 20202021
```

### 5. Controlar un dispositivo

```bash
# Encender bombilla (nodo 1, endpoint 1)
chip-tool onoff on 1 1

# Leer temperatura (nodo 3, endpoint 1)
chip-tool temperaturemeasurement read measured-value 3 1
```

## UI Web

```bash
# Arrancar backend + frontend
./scripts/run-web.sh
```

La UI web estará disponible en `http://localhost:8080`.
