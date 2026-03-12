# Simulación Mock de dispositivos Smart Home

Simulación en memoria de dispositivos Matter para desarrollo y pruebas. No requiere el SDK connectedhomeip ni compilación de binarios C++. Todos los dispositivos se simulan mediante un backend Python (FastAPI) que gestiona el estado en memoria.

## Estructura

```
simulation/
├── scripts/
│   └── run-web.sh     # Arranca el backend (venv + uvicorn)
├── web/
│   ├── backend/
│   │   ├── main.py          # API REST + lógica mock de dispositivos
│   │   └── requirements.txt # FastAPI + uvicorn
│   └── frontend/
│       ├── index.html        # UI web
│       ├── app.js            # Lógica frontend
│       └── style.css         # Estilos
├── start.sh           # Arranca la simulación
└── stop.sh            # Para la simulación
```

## Dispositivos simulados (27)

Al arrancar, los 27 dispositivos aparecen como **disponibles en la red pero no comisionados**. La app debe descubrirlos y comisionarlos usando su `setup_code`.

### Bombillas inteligentes (10)

| Nodo | Nombre | Setup code | Operaciones |
|:---:|---|---|---|
| 1 | Bombilla Salón 1 | `0001-120-1001` | on, off, brightness (0-254), color (hue, saturation), status |
| 2 | Bombilla Salón 2 | `0002-120-1002` | " |
| 3 | Bombilla Salón 3 | `0003-120-1003` | " |
| 4 | Bombilla Cocina 1 | `0004-120-1004` | " |
| 5 | Bombilla Cocina 2 | `0005-120-1005` | " |
| 6 | Bombilla Dormitorio 1 | `0006-120-1006` | " |
| 7 | Bombilla Dormitorio 2 | `0007-120-1007` | " |
| 8 | Bombilla Baño | `0008-120-1008` | " |
| 9 | Bombilla Garaje | `0009-120-1009` | " |
| 10 | Bombilla Pasillo | `0010-120-1010` | " |

### Interruptores on/off (5)

| Nodo | Nombre | Setup code | Operaciones |
|:---:|---|---|---|
| 11 | Interruptor Salón | `0011-120-1011` | on, off, toggle, status |
| 12 | Interruptor Cocina | `0012-120-1012` | " |
| 13 | Interruptor Dormitorio | `0013-120-1013` | " |
| 14 | Interruptor Baño | `0014-120-1014` | " |
| 15 | Interruptor Garaje | `0015-120-1015` | " |

### Cerraduras (2)

| Nodo | Nombre | Setup code | Operaciones |
|:---:|---|---|---|
| 16 | Cerradura Entrada | `0016-120-1016` | lock, unlock, status |
| 17 | Cerradura Garaje | `0017-120-1017` | " |

### Sensor de contacto (1)

| Nodo | Nombre | Setup code | Operaciones |
|:---:|---|---|---|
| 18 | Sensor Contacto Entrada | `0018-120-1018` | status, trigger: contact-open / contact-close |

### Persianas (4)

| Nodo | Nombre | Setup code | Operaciones |
|:---:|---|---|---|
| 19 | Persiana Salón | `0019-120-1019` | set (0-100%), open, close, status |
| 20 | Persiana Cocina | `0020-120-1020` | " |
| 21 | Persiana Dormitorio | `0021-120-1021` | " |
| 22 | Persiana Baño | `0022-120-1022` | " |

### Smart TV + Chromecast (1)

| Nodo | Nombre | Setup code | Operaciones |
|:---:|---|---|---|
| 23 | Smart TV Salón | `0023-120-1023` | on, off, play (url), pause, stop, status |

### Sensor de humos (1)

| Nodo | Nombre | Setup code | Operaciones |
|:---:|---|---|---|
| 24 | Sensor de Humo | `0024-120-1024` | status, trigger: smoke-alarm / smoke-clear |

### Sensor de fugas de agua (1)

| Nodo | Nombre | Setup code | Operaciones |
|:---:|---|---|---|
| 25 | Sensor Fugas Agua | `0025-120-1025` | status, trigger: water-leak / water-clear |

### Sensor de temperatura (1)

| Nodo | Nombre | Setup code | Operaciones |
|:---:|---|---|---|
| 26 | Sensor Temperatura | `0026-120-1026` | lectura (con variación simulada ±0.5°C) |

### Termostato (1)

| Nodo | Nombre | Setup code | Operaciones |
|:---:|---|---|---|
| 27 | Termostato | `0027-120-1027` | read, set temperatura (x100, ej: 2200 = 22.00°C) |

## Uso

### Arrancar

```bash
cd smart-home/simulation
./start.sh
```

- **UI web:** http://localhost:8080
- **API docs:** http://localhost:8080/docs

### Parar

```bash
./stop.sh
```

## API REST

### Descubrimiento y comisionamiento

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/discover` | Lista dispositivos disponibles (no comisionados) con su `setup_code` |
| `POST` | `/api/devices/{node_id}/commission` | Comisiona un dispositivo enviando `{"setup_code": "..."}` |
| `DELETE` | `/api/devices/{node_id}/commission` | Descomisiona un dispositivo |

### Dispositivos comisionados

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/devices` | Lista dispositivos comisionados con estado |
| `GET` | `/api/devices/{node_id}` | Estado de un dispositivo |
| `POST` | `/api/devices/{node_id}/command` | Enviar comando `{"command": "...", "args": [...]}` |
| `POST` | `/api/devices/{node_id}/trigger` | Disparar evento `{"event_type": "..."}` |
| `GET` | `/api/devices/{node_id}/events` | Historial de eventos |

## Flujo de comisionamiento desde la App

### 1. Descubrir dispositivos disponibles

```bash
curl -s http://localhost:8080/api/discover | python3 -m json.tool
```

Respuesta (extracto):

```json
{
    "devices": [
        {"node_id": 1, "type": "lighting", "name": "Bombilla Salón 1", "setup_code": "0001-120-1001"},
        {"node_id": 16, "type": "lock", "name": "Cerradura Entrada", "setup_code": "0016-120-1016"},
        ...
    ]
}
```

### 2. Comisionar un dispositivo con su setup_code

```bash
# Comisionar la Bombilla Salón 1
curl -X POST http://localhost:8080/api/devices/1/commission \
  -H 'Content-Type: application/json' \
  -d '{"setup_code": "0001-120-1001"}'

# Comisionar la Cerradura Entrada
curl -X POST http://localhost:8080/api/devices/16/commission \
  -H 'Content-Type: application/json' \
  -d '{"setup_code": "0016-120-1016"}'
```

Si el código es incorrecto, devuelve `403 Forbidden`.

### 3. Controlar el dispositivo (ya comisionado)

```bash
# Encender bombilla
curl -X POST http://localhost:8080/api/devices/1/command \
  -H 'Content-Type: application/json' \
  -d '{"command": "on", "args": []}'

# Ajustar brillo
curl -X POST http://localhost:8080/api/devices/1/command \
  -H 'Content-Type: application/json' \
  -d '{"command": "brightness", "args": ["200"]}'

# Cambiar color
curl -X POST http://localhost:8080/api/devices/1/command \
  -H 'Content-Type: application/json' \
  -d '{"command": "color", "args": ["120", "200"]}'

# Cerrar cerradura
curl -X POST http://localhost:8080/api/devices/16/command \
  -H 'Content-Type: application/json' \
  -d '{"command": "lock", "args": []}'

# Abrir persiana al 75%
curl -X POST http://localhost:8080/api/devices/19/command \
  -H 'Content-Type: application/json' \
  -d '{"command": "set", "args": ["75"]}'

# Reproducir vídeo en la Smart TV
curl -X POST http://localhost:8080/api/devices/23/command \
  -H 'Content-Type: application/json' \
  -d '{"command": "play", "args": ["https://www.youtube.com/watch?v=dQw4w9WgXcQ"]}'

# Ajustar termostato a 22.5°C
curl -X POST http://localhost:8080/api/devices/27/command \
  -H 'Content-Type: application/json' \
  -d '{"command": "set", "args": ["2250"]}'

# Disparar alarma de humo
curl -X POST http://localhost:8080/api/devices/24/trigger \
  -H 'Content-Type: application/json' \
  -d '{"event_type": "smoke-alarm"}'

# Detectar fuga de agua
curl -X POST http://localhost:8080/api/devices/25/trigger \
  -H 'Content-Type: application/json' \
  -d '{"event_type": "water-leak"}'

# Abrir sensor de contacto (puerta abierta)
curl -X POST http://localhost:8080/api/devices/18/trigger \
  -H 'Content-Type: application/json' \
  -d '{"event_type": "contact-open"}'
```

### 4. Descomisionar un dispositivo

```bash
curl -X DELETE http://localhost:8080/api/devices/1/commission
```
