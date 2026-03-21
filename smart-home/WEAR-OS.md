# Smart Home Wear OS

App companion para Wear OS que permite controlar los dispositivos del hogar desde el smartwatch con acciones rapidas.

## Funcionalidad

La app muestra una lista de dispositivos del hogar agrupados por habitacion. Cada dispositivo accionable tiene un boton de accion rapida (un toque).

### Acciones por tipo de dispositivo

| Tipo | Accion | Indicador |
|------|--------|-----------|
| Luz | Toggle on/off | ON / OFF |
| Cerradura | Lock/Unlock | Icono candado |
| Interruptor | Toggle on/off | ON / OFF |
| Smart TV | Toggle on/off | ON / OFF |
| Persiana | Toggle 0%/100% | Porcentaje |
| Termostato | Toggle calefaccion | Temperatura |
| Sensores | Solo lectura | Estado |

### Pantalla principal

```
    Smart Home Wear

    -- Salon --
    [Luz principal]         ON
    [Smart TV]             OFF

    -- Cocina --
    [Luz cocina]           OFF

    -- Entrada --
    [Puerta principal]      Lock

    -- Otros dispositivos --
    [Luz pasillo]           ON
```

## Arquitectura

```
com.vpedrosa.smarthome.wear/
├── device_control/
│   ├── DeviceControlScreen.kt          # UI (ScalingLazyColumn)
│   ├── DeviceControlViewModel.kt       # Estado + logica
│   ├── model/
│   │   └── WearDevice.kt               # Modelo serializable
│   ├── domain/ports/
│   │   └── DeviceCommandPort.kt        # Puerto de comunicacion
│   └── adapters/
│       ├── WearableDeviceCommandAdapter.kt  # Data Layer API real
│       └── FakeDeviceCommandAdapter.kt      # Datos simulados (fallback)
├── di/WearModule.kt                     # Koin DI
├── theme/WearTheme.kt                   # Tema Material 3
├── MainActivity.kt
└── WearApplication.kt
```

## Comunicacion Watch-Phone

La app se comunica con la app del telefono mediante la **Wearable Data Layer API** (MessageClient).

### Protocolo

| Direccion | Path | Payload |
|-----------|------|---------|
| Watch -> Phone | `/device_list_request` | (vacio) |
| Phone -> Watch | `/device_list_response` | JSON: `{"devices": [...]}` |
| Watch -> Phone | `/device_action` | JSON: `{"deviceId": "x", "action": "TOGGLE"}` |
| Phone -> Watch | `/device_action_result` | `OK:{device JSON}` o `ERROR:msg` |

### Flujo

1. La app se abre y solicita la lista de dispositivos al telefono
2. El telefono serializa dispositivos + habitaciones y responde con JSON
3. El reloj muestra la lista agrupada por habitacion
4. El usuario toca un dispositivo -> se envia la accion al telefono
5. El telefono ejecuta la accion (toggle) y devuelve el estado actualizado
6. El reloj actualiza la UI con el nuevo estado

### Fallback

Si la comunicacion con el telefono falla (timeout, emuladores sin Data Layer), la app cae automaticamente a `FakeDeviceCommandAdapter` que proporciona datos simulados para poder ver la UI.

## Recepcion en el telefono

El telefono tiene dos mecanismos de recepcion:

1. **`WearableMessageListenerService`**: servicio en segundo plano que recibe mensajes cuando la app no esta en primer plano
2. **`WearableMessageHandler`** registrado en `SmartHomeApplication`: listener de primer plano como fallback para emuladores

Ambos delegan al mismo `WearableMessageHandler` que usa los use cases existentes (`ToggleDeviceUseCase`, `DeviceRepository`, `RoomRepository`).

## Limitaciones conocidas

- La **Data Layer API no entrega mensajes entre emuladores** de forma fiable. `NodeClient.connectedNodes` muestra conexion pero `MessageClient` no enruta mensajes. En emuladores se usa el fallback con datos simulados.
- En **dispositivos fisicos** la comunicacion funciona correctamente.

## Decision de diseno

Inicialmente la app usaba **control por voz** (SpeechRecognizer, RecognizerIntent), pero se descarto por:

- `SpeechRecognizer` no disponible en emuladores Wear OS
- `RecognizerIntent` poco fiable en emuladores
- Timeouts frecuentes en la comunicacion de texto reconocido
- Complejidad de gestion del audio en Wear OS

Las **acciones rapidas** son mas practicas para el contexto de un smartwatch: interaccion rapida, sin necesidad de hablar, y funcionamiento fiable.
