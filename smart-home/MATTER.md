# Integración Matter SDK

Documento de decisiones técnicas sobre la integración del protocolo Matter en la aplicación Smart Home.

## Contexto

La aplicación Smart Home controla 27 dispositivos simulados mediante [matter.js](https://github.com/project-chip/matter.js), que implementa el protocolo Matter completo sobre UDP. Los dispositivos virtuales son indistinguibles de hardware real para cualquier controlador Matter.

La app KMP necesita descubrir, comisionar y controlar estos dispositivos usando el SDK Matter de Android.

## Decisiones de arquitectura

### 1. SDK elegido: `matter-android-demo-sdk`

| Opción evaluada | Resultado |
|---|---|
| `com.google.android.gms:play-services-home` | Solo commissioning de alto nivel. No expone `CHIPDeviceController` ni permite control directo de clusters. Requiere Google Home ecosystem para control post-commissioning. |
| Compilar connectedhomeip desde fuente | SDK de 2.6 GB, requiere toolchain C++ completo, tiempos de compilación >10 min por target. Inviable para el entorno de desarrollo del proyecto. |
| `com.google.matter:matter-android-demo-sdk:1.0` | AAR precompilado publicado por Google en Maven Central. Incluye `CHIPDeviceController` + librerías nativas `.so`. Permite commissioning (PASE) y control directo de clusters Matter sobre UDP. |

**Decision:** Se utiliza `matter-android-demo-sdk:1.0`.

- Publicado por Google (Apache 2.0).
- Usado en la [sample app oficial de Google para Matter](https://github.com/google-home/sample-apps-for-matter-android).
- Etiquetado como "demo/development only", lo cual es adecuado para un proyecto academico.
- No requiere compilar codigo nativo ni descargar el repositorio connectedhomeip.

### 2. Commissioning: PASE over IP (sin BLE)

El flujo estandar de commissioning Matter usa **BLE** para el primer contacto con el dispositivo. Esto requiere un dispositivo fisico Android con Bluetooth.

Sin embargo, el protocolo Matter tambien soporta **PASE over IP**: el commissioning se realiza directamente sobre UDP si se conoce la direccion IP y puerto del dispositivo. `CHIPDeviceController` expone este mecanismo:

```kotlin
chipDeviceController.establishPaseConnection(
    deviceId,    // ID local asignado al dispositivo
    ipAddress,   // Direccion IP del dispositivo
    port,        // Puerto UDP
    setupPincode // Passcode del dispositivo
)
```

**Decision:** Se utiliza PASE over IP para el commissioning.

- Los dispositivos matter.js escuchan en puertos UDP conocidos (5540-5566).
- Desde el emulador Android, el host es accesible en `10.0.2.2`.
- Elimina la dependencia de hardware BLE, permitiendo desarrollo en emulador.
- Es un mecanismo estandar del protocolo Matter (no es un hack ni un workaround).

### 3. Descubrimiento: adaptador estatico (emulador)

El descubrimiento de dispositivos Matter en la red local se realiza mediante **mDNS/DNS-SD** (servicio `_matterc._udp.local`). En Android, se implementa con `NsdManager`.

**Problema:** El emulador Android usa NAT, lo que impide que el trafico multicast de mDNS llegue a la red del host. Alternativas evaluadas:

| Opcion | Viabilidad |
|---|---|
| Dispositivo fisico en misma WiFi | mDNS funciona, pero requiere hardware |
| Emulador en modo bridge (QEMU) | Funciona, pero requiere configuracion de red a nivel de sistema (bridge, TAP, permisos root) |
| Discovery estatico | Los dispositivos simulados tienen direcciones y puertos conocidos. Se pueden hardcodear. |

**Decision:** Se implementa un adaptador de descubrimiento estatico (`StaticDeviceDiscoveryAdapter`) que devuelve la lista de dispositivos de la simulacion con sus direcciones conocidas.

Este adaptador implementa el mismo puerto (`DeviceDiscoveryPort`) que implementaria un adaptador mDNS en produccion. Gracias a la inyeccion de dependencias (Koin), el cambio a produccion consiste en sustituir el binding:

```kotlin
// Desarrollo (emulador)
single<DeviceDiscoveryPort> { StaticDeviceDiscoveryAdapter() }

// Produccion (dispositivo fisico)
single<DeviceDiscoveryPort> { MdnsDeviceDiscoveryAdapter(androidContext()) }
```

### 4. Arquitectura de puertos y adaptadores

La integracion sigue la **arquitectura hexagonal** existente en la app. Los puertos se definen en `commonMain` (Kotlin Multiplatform) y los adaptadores en `androidMain` o `commonMain` segun la dependencia de plataforma:

```
commonMain (puertos)                     androidMain (adaptadores)
┌──────────────────────┐                ┌────────────────────────────────┐
│ DeviceDiscoveryPort  │◄───────────────│ StaticDeviceDiscoveryAdapter   │
│                      │                │ (hardcoded, emulador)          │
│                      │    futuro      │                                │
│                      │◄──────────── ─ │ MdnsDeviceDiscoveryAdapter     │
│                      │                │ (NsdManager, produccion)       │
└──────────────────────┘                └────────────────────────────────┘

┌──────────────────────┐                ┌────────────────────────────────┐
│ CommissioningPort    │◄───────────────│ MatterCommissioningAdapter     │
│                      │                │ (CHIPDeviceController)         │
└──────────────────────┘                └────────────────────────────────┘

┌──────────────────────┐                ┌────────────────────────────────┐
│ DeviceControlPort    │◄───────────────│ MatterDeviceControlAdapter     │
│                      │                │ (CHIPClusters.*)               │
└──────────────────────┘                └────────────────────────────────┘
```

### 5. Control de dispositivos: clusters Matter

Tras el commissioning, `CHIPDeviceController` permite enviar comandos a los clusters Matter estandar del dispositivo:

| Cluster Matter | Dispositivos | Operaciones |
|---|---|---|
| `OnOff` | Bombillas, interruptores, Smart TV | on, off, toggle |
| `LevelControl` | Bombillas | moveToLevel (brillo) |
| `DoorLock` | Cerraduras | lockDoor, unlockDoor |
| `WindowCovering` | Persianas | goToLiftPercentage |
| `Thermostat` | Termostato | setpointRaiseLower |
| `BooleanState` | Sensor contacto | lectura (suscripcion) |
| `SmokeCoAlarm` | Sensor humo | lectura (suscripcion) |
| `WaterLeakDetector` | Sensor fugas | lectura (suscripcion) |
| `TemperatureMeasurement` | Sensor temperatura | lectura (suscripcion) |

## Flujo completo

```
┌─────────────┐    mDNS/estatico    ┌─────────────────┐
│  matter.js   │◄──────────────────│  App Android      │
│  (27 devices)│    PASE over IP   │                   │
│              │◄──────────────────│  CHIPDevice-       │
│  UDP :5540-  │    Matter/UDP     │  Controller       │
│       :5566  │◄──────────────────│                   │
└─────────────┘                    └─────────────────┘
     HOST                            EMULADOR
   (simulacion)                    (10.0.2.2 → host)
```

1. **Descubrimiento**: El adaptador estatico proporciona la lista de dispositivos con IP `10.0.2.2` y puertos 5540-5566.
2. **Commissioning**: `CHIPDeviceController.establishPaseConnection()` realiza PASE sobre UDP con el passcode del dispositivo.
3. **Control**: `CHIPClusters.*` envia comandos Matter reales al dispositivo a traves del mismo canal UDP.
4. **Suscripciones**: La app se suscribe a cambios de atributos (sensores) para recibir actualizaciones en tiempo real.

## Preparacion para produccion

Para pasar a produccion con dispositivos reales, los cambios son exclusivamente en la capa de adaptadores:

| Componente | Desarrollo | Produccion |
|---|---|---|
| Discovery | `StaticDeviceDiscoveryAdapter` | `MdnsDeviceDiscoveryAdapter` (NsdManager) |
| Commissioning | PASE over IP (10.0.2.2) | PASE over BLE + IP |
| Control | Sin cambios | Sin cambios |
| Permisos | `INTERNET` | `INTERNET` + `BLUETOOTH_SCAN` + `BLUETOOTH_CONNECT` + `ACCESS_FINE_LOCATION` |

Ningun use case, view model ni pantalla necesita modificacion. La inyeccion de dependencias (Koin) gestiona el intercambio de adaptadores.

## Referencias

- [Matter Specification](https://csa-iot.org/developer-resource/specifications-download-request/)
- [matter.js (simulacion)](https://github.com/project-chip/matter.js)
- [Google Sample App for Matter](https://github.com/google-home/sample-apps-for-matter-android)
- [matter-android-demo-sdk (Maven)](https://central.sonatype.com/artifact/com.google.matter/matter-android-demo-sdk)
- [connectedhomeip (CHIP)](https://github.com/project-chip/connectedhomeip)
- [PASE over IP (Matter spec 4.13)](https://csa-iot.org/developer-resource/specifications-download-request/)
