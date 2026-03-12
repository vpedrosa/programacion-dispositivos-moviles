# Simulacion Matter (matter.js)

Simulacion de 27 dispositivos Matter virtuales usando [matter.js](https://github.com/project-chip/matter.js). Cada dispositivo es un `ServerNode` independiente que usa el protocolo Matter real sobre UDP (mDNS, PASE, MRP). Son indistinguibles de hardware fisico para cualquier controlador Matter.

## Estructura

```
simulation/
├── src/
│   ├── main.mjs              # Entrada: arranca los 27 dispositivos
│   ├── config.mjs             # Definicion de dispositivos (puertos, discriminators, passcodes)
│   └── devices/
│       ├── lighting.mjs       # DimmableLightDevice (on/off + brillo)
│       ├── switch.mjs         # OnOffPlugInUnitDevice
│       ├── lock.mjs           # DoorLockDevice
│       ├── contact-sensor.mjs # ContactSensorDevice
│       ├── window.mjs         # WindowCoveringDevice (posicion %)
│       ├── media-player.mjs   # BasicVideoPlayerDevice
│       ├── smoke.mjs          # SmokeCoAlarmDevice
│       ├── water-leak.mjs     # WaterLeakDetectorDevice
│       ├── temperature.mjs    # TemperatureSensorDevice (variacion simulada)
│       └── thermostat.mjs     # ThermostatDevice (calefaccion)
├── package.json
├── start.sh                   # Arranca la simulacion en background
└── stop.sh                    # Para la simulacion
```

## Dispositivos simulados (27)

Al arrancar, los 27 dispositivos se anuncian por **mDNS** como no comisionados. La app los descubre y comisiona usando el protocolo Matter estandar (PASE sobre UDP).

Cada dispositivo muestra su **manual pairing code** y **QR code** en el log al arrancar.

### Bombillas inteligentes (10) — DimmableLight

| # | Nombre | Puerto | Discriminator | Passcode |
|:-:|--------|:------:|:-------------:|:--------:|
| 1 | Bombilla Salon 1 | 5540 | 3840 | 20202021 |
| 2 | Bombilla Salon 2 | 5541 | 3841 | 20202022 |
| 3 | Bombilla Salon 3 | 5542 | 3842 | 20202023 |
| 4 | Bombilla Cocina 1 | 5543 | 3843 | 20202024 |
| 5 | Bombilla Cocina 2 | 5544 | 3844 | 20202025 |
| 6 | Bombilla Dormitorio 1 | 5545 | 3845 | 20202026 |
| 7 | Bombilla Dormitorio 2 | 5546 | 3846 | 20202027 |
| 8 | Bombilla Bano | 5547 | 3847 | 20202028 |
| 9 | Bombilla Garaje | 5548 | 3848 | 20202029 |
| 10 | Bombilla Pasillo | 5549 | 3849 | 20202030 |

### Interruptores on/off (5) — OnOffPlugInUnit

| # | Nombre | Puerto | Discriminator | Passcode |
|:-:|--------|:------:|:-------------:|:--------:|
| 11 | Interruptor Salon | 5550 | 3850 | 20202031 |
| 12 | Interruptor Cocina | 5551 | 3851 | 20202032 |
| 13 | Interruptor Dormitorio | 5552 | 3852 | 20202033 |
| 14 | Interruptor Bano | 5553 | 3853 | 20202034 |
| 15 | Interruptor Garaje | 5554 | 3854 | 20202035 |

### Cerraduras (2) — DoorLock

| # | Nombre | Puerto | Discriminator | Passcode |
|:-:|--------|:------:|:-------------:|:--------:|
| 16 | Cerradura Entrada | 5555 | 3855 | 20202036 |
| 17 | Cerradura Garaje | 5556 | 3856 | 20202037 |

### Sensor de contacto (1) — ContactSensor

| # | Nombre | Puerto | Discriminator | Passcode |
|:-:|--------|:------:|:-------------:|:--------:|
| 18 | Sensor Contacto Entrada | 5557 | 3857 | 20202038 |

### Persianas (4) — WindowCovering

| # | Nombre | Puerto | Discriminator | Passcode |
|:-:|--------|:------:|:-------------:|:--------:|
| 19 | Persiana Salon | 5558 | 3858 | 20202039 |
| 20 | Persiana Cocina | 5559 | 3859 | 20202040 |
| 21 | Persiana Dormitorio | 5560 | 3860 | 20202041 |
| 22 | Persiana Bano | 5561 | 3861 | 20202042 |

### Smart TV (1) — BasicVideoPlayer

| # | Nombre | Puerto | Discriminator | Passcode |
|:-:|--------|:------:|:-------------:|:--------:|
| 23 | Smart TV Salon | 5562 | 3862 | 20202043 |

### Sensor de humo (1) — SmokeCoAlarm

| # | Nombre | Puerto | Discriminator | Passcode |
|:-:|--------|:------:|:-------------:|:--------:|
| 24 | Sensor de Humo | 5563 | 3863 | 20202044 |

### Sensor de fugas de agua (1) — WaterLeakDetector

| # | Nombre | Puerto | Discriminator | Passcode |
|:-:|--------|:------:|:-------------:|:--------:|
| 25 | Sensor Fugas Agua | 5564 | 3864 | 20202045 |

### Sensor de temperatura (1) — TemperatureSensor

| # | Nombre | Puerto | Discriminator | Passcode |
|:-:|--------|:------:|:-------------:|:--------:|
| 26 | Sensor Temperatura | 5565 | 3865 | 20202046 |

### Termostato (1) — Thermostat (Heating)

| # | Nombre | Puerto | Discriminator | Passcode |
|:-:|--------|:------:|:-------------:|:--------:|
| 27 | Termostato | 5566 | 3866 | 20202047 |

## Uso

### Arrancar

```bash
cd smart-home/simulation
./start.sh
```

La primera vez instala dependencias (`npm install`). Los dispositivos se anuncian por mDNS en la red local.

### Ver logs

```bash
tail -f logs/matter.log
```

El log muestra el QR code y manual pairing code de cada dispositivo.

### Parar

```bash
./stop.sh
```

### Resetear estado

Los dispositivos guardan estado de comisionamiento en `~/.matter/SIM-*`. Para resetear:

```bash
rm -rf ~/.matter/SIM-*
```

## Comisionamiento desde la app

1. Arranca la simulacion (`./start.sh`)
2. Abre la app en un dispositivo/emulador **en la misma red**
3. La app descubre los dispositivos por mDNS
4. Usa el **manual pairing code** o **QR code** del log para comisionar cada dispositivo
5. Una vez comisionado, el dispositivo se controla con el protocolo Matter estandar
