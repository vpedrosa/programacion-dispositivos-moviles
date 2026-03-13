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

### 2. Sesiones PASE over IP (sin BLE ni CASE)

#### Flujo Matter estandar vs. implementacion en emulador

El flujo completo de commissioning Matter consta de varias fases:

```
PASE (autenticacion por passcode)
  → NOC (instalar certificados operacionales)
    → FindOperational (descubrir dispositivo via mDNS con su identidad operacional)
      → CASE (establecer sesion operacional con certificados)
        → Cluster Commands (control del dispositivo)
```

En el emulador Android, **FindOperational falla** porque utiliza mDNS (`_matter._tcp`) y el emulador usa NAT, que impide el trafico multicast. El SDK intenta resolver la direccion operacional durante ~45 segundos antes de dar timeout (error 0x32). Esto provoca que `commissionDevice()` falle en el paso `FindOperational → Cleanup` y el dispositivo revierta la fabric instalada.

#### PASE vs CASE

| | PASE | CASE |
|---|---|---|
| Autenticacion | Passcode (SPAKE2+) | Certificados (NOC) |
| Cifrado | AES-CCM (sesion derivada) | AES-CCM (sesion derivada) |
| Canal seguro | Si | Si |
| Cluster commands | Si | Si |
| Requiere mDNS | No | Si |
| Diseñado para | Commissioning | Operacion permanente |
| Limitacion | Requiere conocer passcode | Requiere commissioning completo |

Ambos tipos de sesion son **canales autenticados y cifrados del protocolo Matter**. La diferencia es el mecanismo de autenticacion (passcode vs certificados), no la seguridad del canal ni los comandos que se pueden enviar.

#### Decision

Se utiliza **PASE over IP** tanto para verificar conectividad como para el control de dispositivos, sin llamar a `commissionDevice()`:

```kotlin
// Commissioning: solo PASE para verificar conectividad
chipDeviceController.establishPaseConnection(nodeId, ip, port, passcode)

// Control: re-establecer PASE y enviar cluster commands
chipDeviceController.establishPaseConnection(nodeId, ip, port, passcode)
val pointer = chipDeviceController.getDeviceBeingCommissionedPointer(nodeId)
ChipClusters.OnOffCluster(pointer, endpoint).on(callback)
```

- Los dispositivos matter.js escuchan en puertos UDP conocidos (5540-5566).
- Desde el emulador Android, el host es accesible en `10.0.2.2`.
- Elimina la dependencia de hardware BLE y de mDNS, permitiendo desarrollo completo en emulador.
- PASE es un mecanismo estandar del protocolo Matter (RFC, no workaround).
- Los comandos de cluster viajan cifrados sobre la sesion PASE igual que lo harian sobre CASE.

#### Preparacion para CASE en produccion

En un entorno con red local real (mDNS funciona), el adapter de control se reemplazaria por uno que use `commissionDevice()` + `getConnectedDevicePointer()` (CASE). Ningun cambio en puertos, use cases ni UI gracias a la arquitectura hexagonal.

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

Tras establecer la sesion PASE, `MatterDeviceControlAdapter` re-establece PASE con la direccion conocida del dispositivo y envia comandos a los clusters Matter estandar:

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
2. **Commissioning (PASE)**: `CHIPDeviceController.establishPaseConnection()` realiza PASE (SPAKE2+) sobre UDP con el passcode del dispositivo. Verifica conectividad y establece sesion cifrada. No se llama a `commissionDevice()` (ver seccion 2).
3. **Registro**: `DeviceControlPort.registerDevice()` almacena la informacion de conexion (host, puerto, passcode) para poder re-establecer PASE en operaciones de control posteriores.
4. **Control**: Para cada comando, `MatterDeviceControlAdapter` re-establece PASE con la direccion conocida, obtiene el device pointer via `getDeviceBeingCommissionedPointer()`, y envia el cluster command (`ChipClusters.*`) sobre la sesion PASE cifrada.
5. **Suscripciones**: (Pendiente) La app se suscribiria a cambios de atributos (sensores) para recibir actualizaciones en tiempo real.

## Preparacion para produccion

Para pasar a produccion con dispositivos reales, los cambios son exclusivamente en la capa de adaptadores:

| Componente | Desarrollo (emulador) | Produccion (dispositivo fisico) |
|---|---|---|
| Discovery | `StaticDeviceDiscoveryAdapter` | `MdnsDeviceDiscoveryAdapter` (NsdManager) |
| Commissioning | PASE over IP (solo verificacion) | PASE + `commissionDevice()` (NOC + CASE) |
| Control | PASE directo (`getDeviceBeingCommissionedPointer`) | CASE operacional (`getConnectedDevicePointer`) |
| Permisos | `INTERNET`, `ACCESS_WIFI_STATE`, `CHANGE_WIFI_MULTICAST_STATE` | + `BLUETOOTH_SCAN` + `BLUETOOTH_CONNECT` + `ACCESS_FINE_LOCATION` |

Ningun use case, view model ni pantalla necesita modificacion. La inyeccion de dependencias (Koin) gestiona el intercambio de adaptadores.

## Referencias

- [Matter Specification](https://csa-iot.org/developer-resource/specifications-download-request/)
- [matter.js (simulacion)](https://github.com/project-chip/matter.js)
- [Google Sample App for Matter](https://github.com/google-home/sample-apps-for-matter-android)
- [matter-android-demo-sdk (Maven)](https://central.sonatype.com/artifact/com.google.matter/matter-android-demo-sdk)
- [connectedhomeip (CHIP)](https://github.com/project-chip/connectedhomeip)
- [PASE over IP (Matter spec 4.13)](https://csa-iot.org/developer-resource/specifications-download-request/)
