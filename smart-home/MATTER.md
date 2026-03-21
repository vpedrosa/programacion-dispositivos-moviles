# Integracion Matter - Limitaciones y decisiones

Este documento describe como la app interactua con el protocolo Matter, las limitaciones del SDK de demo y las decisiones de diseno tomadas.

## Arquitectura de comunicacion

```
┌─────────────────────┐          PASE           ┌─────────────────────┐
│   App Android       │◄────────────────────────►│  Simulador Matter   │
│   (ChipDeviceCtrl)  │   passcode + host:port   │  (matter.js)        │
└─────────────────────┘                          └─────────────────────┘
```

La app se comunica con el simulador Matter usando **sesiones PASE** (Passcode-Authenticated Session Establishment). No se usa CASE (Certificate Authenticated Session Establishment).

## PASE vs CASE

| | PASE | CASE |
|---|---|---|
| **Proposito** | Comisionamiento (emparejar) | Control continuo |
| **Duracion** | Temporal (minutos) | Larga duracion |
| **Autenticacion** | Passcode (setup code) | Certificados (fabric/NOC) |
| **Estado en el proyecto** | Unico metodo usado | No soportado |

### Por que no se usa CASE

1. **FindOperational (mDNS/CASE) no funciona en el emulador Android** - el descubrimiento operacional post-comisionamiento falla
2. **No se hace comisionamiento completo** - la app usa `establishPaseConnection()` en lugar de `commissionDevice()`, que seria necesario para establecer fabric y certificados NOC
3. **SDK de demo** (`matter-android-demo-sdk:1.0`) tiene limitaciones frente al SDK completo de produccion

## Flujo de comisionamiento

```
1. Usuario pulsa "Comisionar" en la app
2. MatterCommissioningAdapter.commission(device)
   └── establishPaseConnection(nodeId, host, port, passcode)
       └── chipController.establishPaseConnection(...)
           └── onPairingComplete(code=0) → PASE OK
3. Se cachea el device pointer para uso posterior
4. Se crea el Device en memoria (Light, Lock, etc.)
```

**Nota:** No se llama a `commissionDevice()` porque `FindOperational` no funciona en el emulador. El "comisionamiento" es en realidad solo una verificacion de conectividad PASE.

## Flujo de control de dispositivos

```
1. Usuario interactua con un dispositivo (toggle, slider, etc.)
2. ToggleDeviceUseCase → deviceControlPort.toggleOnOff(id, on)
3. MatterDeviceControlAdapter.executeWithRetry(deviceId)
   ├── Intenta con el pointer cacheado
   ├── Si falla (sesion expirada):
   │   ├── Invalida cache
   │   ├── Espera 1s
   │   ├── Crea PASE nueva con nodeId temporal (fuerza sesion fresca)
   │   └── Reintenta el comando
   └── Si el retry tambien falla → lanza excepcion
4. ToggleDeviceUseCase captura la excepcion
   └── Si Matter falla, el estado NO se actualiza en memoria
```

## Expiracion de sesiones PASE

Las sesiones PASE son temporales por diseno. Cuando el simulador las expira (por inactividad), los comandos fallan con `ChipClusterException: CHIP cluster error: 1` y el simulador reporta `Ignoring message for unknown session`.

### Problema del re-establecimiento

`chipController.establishPaseConnection(nodeId)` con el **mismo nodeId** no crea una sesion nueva. El controller internamente reutiliza la sesion existente (mismo LSID), aunque el simulador ya la olvido.

### Solucion: nodeId temporal

El retry usa un nodeId temporal incremental (`100000+`) que fuerza al controller a crear una sesion PASE completamente nueva. El simulador acepta la conexion basandose en el passcode, independientemente del nodeId. El pointer resultante se cachea contra el nodeId original del dispositivo.

### Timeout de comandos

Los comandos Matter tienen un timeout de 3 segundos. Si el SDK no recibe respuesta en ese tiempo, el comando falla inmediatamente y se activa el retry con sesion nueva. Sin este timeout, el SDK reintenta internamente 4 veces con backoff exponencial (~15 segundos).

## Simulacion de eventos de sensores

Los eventos de sensores (temperatura, humo, fugas, contacto de puerta) son **generados localmente** por el `SensorEventSimulator` dentro de la app. **No provienen del simulador Matter externo.**

```
SensorEventSimulator (en la app)
├── emitTemperatureReadings()    → valores aleatorios cada 3s
├── emitContactSensorEvents()   → toggle puerta cada 10s
├── emitSmokeAlerts()           → 50% probabilidad cada 5s
├── emitWaterLeakAlerts()       → 50% probabilidad cada 5s
├── emitThermostatAdjustments() → ajustes aleatorios cada 2min
└── monitorDoorOpenDuration()   → alerta si puerta abierta >2min
```

### Por que la simulacion es local

Para recibir eventos reales del simulador Matter se necesitarian **suscripciones a atributos** (attribute subscriptions), que requieren sesiones CASE activas. Como CASE no esta soportado, los eventos se simulan localmente:

- El `SensorEventSimulator` genera eventos periodicos y los guarda en `DeviceEventRepository`
- Para sensores de contacto, ademas **modifica el estado del dispositivo** (`sensor.toggle()` + `deviceRepository.save()`) para simular apertura/cierre de puertas
- Los valores de temperatura son aleatorios (18-28 C), no lecturas reales del simulador

### Que se comunica realmente con Matter

| Operacion | Usa Matter? | Descripcion |
|-----------|-------------|-------------|
| Comisionamiento | Si (PASE) | Verifica conectividad con el simulador |
| Toggle luz/switch/TV | Si (PASE) | Envia comando OnOff al simulador |
| Lock/Unlock cerradura | Si (PASE) | Envia comando DoorLock al simulador |
| Ajustar termostato | Si (PASE) | Envia setpoint/modo al simulador |
| Mover persiana | Si (PASE) | Envia posicion WindowCovering al simulador |
| Eventos de sensores | No | Generados localmente por SensorEventSimulator |
| Notificaciones push | No | Generadas localmente por AndroidNotificationAdapter |
| Lectura de temperatura | No | Valor aleatorio, no lectura real |
| Estado de puerta | No | Toggle automatico local, no lectura real |

## Archivos clave

| Archivo | Responsabilidad |
|---------|-----------------|
| `MatterControllerProvider.kt` | Crea el `ChipDeviceController` singleton |
| `MatterCommissioningAdapter.kt` | Establece PASE durante comisionamiento |
| `MatterDeviceControlAdapter.kt` | Envia comandos a dispositivos via PASE |
| `SensorEventSimulator.kt` | Genera eventos de sensores localmente |
| `StaticDeviceDiscoveryAdapter.kt` | Lista dispositivos disponibles para comisionar |
