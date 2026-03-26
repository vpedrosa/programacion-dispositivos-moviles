# Smart Home

Aplicación multiplataforma de gestión de casa inteligente con **Kotlin Multiplatform** (Compose Multiplatform). Dispositivos Matter simulados, modo antiokupas y control remoto desde Wear OS.

## Estructura

```
smart-home/
├── app/                    # App KMP (Android + Wear OS)
│   ├── composeApp/         # Código compartido (commonMain)
│   ├── androidApp/         # Entry point Android
│   └── wearApp/            # App Wear OS (acciones rápidas)
├── simulation/             # Simulación Matter (connectedhomeip)
│   ├── scripts/            # Scripts de setup, build y control
│   ├── web/                # Backend (FastAPI) + Frontend
│   ├── start.sh / stop.sh  # Orquestador
│   └── third_party/        # SDK connectedhomeip (submódulo)
└── prototipo/              # Prototipo visual (.pen)
```

## Requisitos

- **App:** JDK 11+, Android SDK API 36, Android Studio Meerkat+
- **Simulación:** Linux x64, Python 3.10+, GCC, Ninja

## App Android

```bash
cd smart-home/app
./gradlew :androidApp:assembleDebug      # compilar
./gradlew :androidApp:installDebug       # instalar en dispositivo/emulador
```

## App Wear OS

La app del smartwatch muestra una lista de dispositivos agrupados por habitación con acciones rápidas de un toque (toggle on/off, lock/unlock, abrir/cerrar persianas, etc.). Se comunica con la app del teléfono via Google Wearable Data Layer API.

```bash
cd smart-home/app
./gradlew :wearApp:assembleDebug
./gradlew :wearApp:installDebug          # requiere emulador Wear OS API 35+
```

Ver [WEAR-OS.md](WEAR-OS.md) para documentación detallada.

## Tests

```bash
cd smart-home/app
./gradlew :composeApp:testDebugUnitTest  # 93+ tests unitarios
./gradlew :wearApp:testDebugUnitTest     # tests Wear OS
```

## Simulación Matter

Ver [MATTER.md](MATTER.md) para documentación detallada sobre la integración Matter, limitaciones del SDK y decisiones de diseño.


```bash
# Setup inicial (una sola vez)
git submodule update --init smart-home/simulation/third_party/connectedhomeip
cd smart-home/simulation
./scripts/setup.sh          # instala dependencias del sistema
./scripts/build.sh          # compila dispositivos y chip-tool

# Arrancar simulación completa (dispositivos + backend + UI web)
./start.sh
# Solo algunos: ./start.sh --devices lighting,lock,thermostat

# UI web en http://localhost:8080
# API docs en http://localhost:8080/docs

# Parar
./stop.sh
```

### Control manual

```bash
./scripts/control.sh lighting on 1          # encender bombilla
./scripts/control.sh lock lock 2            # cerrar cerradura
./scripts/control.sh thermostat set 5 2200  # termostato a 22°C
./scripts/control.sh trigger smoke-alarm 3  # disparar alarma de humo
```

## Control por voz

La app soporta comandos de voz en **espanol** e **ingles** mediante reconocimiento de voz nativo. Se accede desde la pantalla de control por voz.

### Luces

| Accion | Espanol | Ingles |
|--------|---------|--------|
| Encender | "Enciende las luces" | "Turn on the lights" |
| Apagar | "Apaga las luces" | "Turn off the lights" |
| En habitacion | "Enciende las luces del salon" | "Turn on the lights in the living room" |

### Persianas

| Accion | Espanol | Ingles |
|--------|---------|--------|
| Abrir | "Abre las persianas" | "Open the blinds" |
| Cerrar | "Cierra las persianas" | "Close the blinds" |
| En habitacion | "Abre las persianas del dormitorio" | "Open the blinds in the bedroom" |

### Cerraduras

| Accion | Espanol | Ingles |
|--------|---------|--------|
| Cerrar | "Cierra la puerta" | "Lock the door" |
| Abrir | "Abre la puerta" | "Unlock the door" |
| Puerta especifica | "Cierra la puerta de entrada" | "Lock the door of garage" |

### Termostato

| Accion | Espanol | Ingles |
|--------|---------|--------|
| Ajustar | "Pon la temperatura a 22 grados" | "Set temperature to 22 degrees" |
| En habitacion | "Sube la temperatura a 24 grados del salon" | "Set temperature to 24 in the living room" |

### TV / Interruptores

| Accion | Espanol | Ingles |
|--------|---------|--------|
| Encender TV | "Enciende la television" | "Turn on the TV" |
| Apagar TV | "Apaga la tele" | "Turn off the TV" |
| Interruptores | "Enciende los interruptores" | "Turn on the switches" |

> **Nota:** los comandos soportan variaciones naturales (ej. "activa", "prende", "switch on"). Se puede especificar habitacion con "del/de la/in the" + nombre.

## Stack

Kotlin 2.3, Compose Multiplatform 1.10, Material 3, Koin 4.1, kotlinx.serialization, Jetpack Navigation, Wear Compose Material 3, Google Wearable Data Layer API, connectedhomeip (Matter), FastAPI.

Arquitectura hexagonal con vertical slicing. i18n en ingles y espanol (23 strings de accesibilidad).
