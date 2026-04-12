# Smart Home

Aplicación multiplataforma de gestión de casa inteligente con **Kotlin Multiplatform** (Compose Multiplatform). Dispositivos Matter simulados, modo antiokupas y control remoto desde Wear OS.

## Estructura

```
smart-home/
├── app/                    # App KMP (Android + Wear OS)
│   ├── composeApp/         # Código compartido (commonMain)
│   ├── androidApp/         # Entry point Android
│   └── wearApp/            # App Wear OS (acciones rápidas)
├── simulation/             # Simulación Matter (matter.js / Node.js)
│   ├── src/                # Simulador Matter (main.mjs, ws-server.mjs, config.mjs)
│   ├── dashboard/          # Dashboard web (Next.js)
│   ├── start.sh / stop.sh  # Orquestador interno
│   └── patches/            # Parches al SDK matter.js
├── launch.sh               # Script principal de arranque (simulación + dashboard)
└── prototipo/              # Prototipo visual (.pen)
```

## Requisitos

- **App:** JDK 11+, Android SDK API 36, Android Studio Meerkat+
- **Simulación:** Node.js 20+, npm

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
./gradlew :composeApp:testDebugUnitTest  # 140+ tests unitarios
./gradlew :wearApp:testDebugUnitTest     # tests Wear OS
```

## Simulación Matter

Ver [MATTER.md](MATTER.md) para documentación detallada sobre la integración Matter, limitaciones del SDK y decisiones de diseño.

El simulador levanta 27 dispositivos Matter (bombillas, cerraduras, persianas, sensores…) mediante matter.js. No requiere compilar el SDK nativo.

```bash
# Setup inicial (una sola vez, instala node_modules automáticamente)
cd smart-home

# Arrancar simulación + dashboard web
./launch.sh

# Dashboard en http://localhost:3000
# Simulador Matter en puertos UDP 5540-5566, WebSocket en 8085

# Ctrl+C para parar ambos
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

Kotlin 2.3, Compose Multiplatform 1.10, Material 3, Koin 4.1, kotlinx.serialization, Jetpack Navigation, Wear Compose Material 3, Google Wearable Data Layer API, matter.js (simulador Matter), Next.js (dashboard).

Arquitectura hexagonal con vertical slicing. i18n en ingles y espanol (23 strings de accesibilidad).
