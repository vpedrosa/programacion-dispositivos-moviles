"""Backend API REST — Simulación mock de dispositivos Matter para Smart Home."""

import random
from contextlib import asynccontextmanager
from datetime import datetime
from pathlib import Path
from typing import Any

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel

# --- Estado en memoria ---

# Dispositivos disponibles en la red (aún no comisionados).
# Cada uno tiene un setup_code que la app debe enviar para comisionarlo.
available_devices: dict[int, dict[str, Any]] = {}

# Dispositivos comisionados (controlables).
commissioned_devices: dict[int, dict[str, Any]] = {}

# Historial de eventos.
event_history: list[dict[str, Any]] = []

# fmt: off
DEFAULT_DEVICES = [
    # --- Bombillas inteligentes (10) ---
    {"node_id": 1,  "type": "lighting", "name": "Bombilla Salón 1",      "setup_code": "0001-120-1001"},
    {"node_id": 2,  "type": "lighting", "name": "Bombilla Salón 2",      "setup_code": "0002-120-1002"},
    {"node_id": 3,  "type": "lighting", "name": "Bombilla Salón 3",      "setup_code": "0003-120-1003"},
    {"node_id": 4,  "type": "lighting", "name": "Bombilla Cocina 1",     "setup_code": "0004-120-1004"},
    {"node_id": 5,  "type": "lighting", "name": "Bombilla Cocina 2",     "setup_code": "0005-120-1005"},
    {"node_id": 6,  "type": "lighting", "name": "Bombilla Dormitorio 1", "setup_code": "0006-120-1006"},
    {"node_id": 7,  "type": "lighting", "name": "Bombilla Dormitorio 2", "setup_code": "0007-120-1007"},
    {"node_id": 8,  "type": "lighting", "name": "Bombilla Baño",         "setup_code": "0008-120-1008"},
    {"node_id": 9,  "type": "lighting", "name": "Bombilla Garaje",       "setup_code": "0009-120-1009"},
    {"node_id": 10, "type": "lighting", "name": "Bombilla Pasillo",      "setup_code": "0010-120-1010"},
    # --- Interruptores on/off (5) ---
    {"node_id": 11, "type": "switch", "name": "Interruptor Salón",      "setup_code": "0011-120-1011"},
    {"node_id": 12, "type": "switch", "name": "Interruptor Cocina",     "setup_code": "0012-120-1012"},
    {"node_id": 13, "type": "switch", "name": "Interruptor Dormitorio", "setup_code": "0013-120-1013"},
    {"node_id": 14, "type": "switch", "name": "Interruptor Baño",       "setup_code": "0014-120-1014"},
    {"node_id": 15, "type": "switch", "name": "Interruptor Garaje",     "setup_code": "0015-120-1015"},
    # --- Cerraduras (2) ---
    {"node_id": 16, "type": "lock", "name": "Cerradura Entrada", "setup_code": "0016-120-1016"},
    {"node_id": 17, "type": "lock", "name": "Cerradura Garaje",  "setup_code": "0017-120-1017"},
    # --- Sensor de contacto (1) ---
    {"node_id": 18, "type": "contact_sensor", "name": "Sensor Contacto Entrada", "setup_code": "0018-120-1018"},
    # --- Persianas (4) ---
    {"node_id": 19, "type": "window", "name": "Persiana Salón",      "setup_code": "0019-120-1019"},
    {"node_id": 20, "type": "window", "name": "Persiana Cocina",     "setup_code": "0020-120-1020"},
    {"node_id": 21, "type": "window", "name": "Persiana Dormitorio", "setup_code": "0021-120-1021"},
    {"node_id": 22, "type": "window", "name": "Persiana Baño",       "setup_code": "0022-120-1022"},
    # --- Smart TV + Chromecast (1) ---
    {"node_id": 23, "type": "media_player", "name": "Smart TV Salón", "setup_code": "0023-120-1023"},
    # --- Sensor de humos (1) ---
    {"node_id": 24, "type": "smoke", "name": "Sensor de Humo", "setup_code": "0024-120-1024"},
    # --- Sensor de fugas de agua (1) ---
    {"node_id": 25, "type": "water_leak", "name": "Sensor Fugas Agua", "setup_code": "0025-120-1025"},
    # --- Sensor de temperatura (1) ---
    {"node_id": 26, "type": "temperature", "name": "Sensor Temperatura", "setup_code": "0026-120-1026"},
    # --- Termostato (1) ---
    {"node_id": 27, "type": "thermostat", "name": "Termostato", "setup_code": "0027-120-1027"},
]
# fmt: on


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Carga los dispositivos como disponibles (no comisionados)."""
    for dev in DEFAULT_DEVICES:
        available_devices[dev["node_id"]] = {
            "type": dev["type"],
            "name": dev["name"],
            "setup_code": dev["setup_code"],
        }
    yield


app = FastAPI(
    title="Smart Home Simulation API (Mock)",
    version="2.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


# --- Modelos ---


class CommandRequest(BaseModel):
    cluster: str = ""
    command: str
    args: list[str] = []


class TriggerRequest(BaseModel):
    event_type: str  # "smoke-alarm", "water-leak", "contact-open", "contact-close"


class CommissionRequest(BaseModel):
    setup_code: str


# --- Descubrimiento y comisionamiento ---


@app.get("/api/discover")
async def discover_devices():
    """Lista dispositivos disponibles en la red (no comisionados aún).

    Devuelve node_id, type, name y setup_code de cada dispositivo pendiente.
    """
    devices = []
    for node_id, info in available_devices.items():
        if node_id not in commissioned_devices:
            devices.append({
                "node_id": node_id,
                "type": info["type"],
                "name": info["name"],
                "setup_code": info["setup_code"],
            })
    return {"devices": devices}


@app.post("/api/devices/{node_id}/commission")
async def commission_device(node_id: int, request: CommissionRequest):
    """Comisiona un dispositivo usando su setup_code.

    Valida el código y mueve el dispositivo de 'disponible' a 'comisionado'.
    """
    if node_id not in available_devices:
        raise HTTPException(404, f"Device {node_id} not found in network")

    if node_id in commissioned_devices:
        raise HTTPException(409, f"Device {node_id} is already commissioned")

    expected_code = available_devices[node_id]["setup_code"]
    if request.setup_code != expected_code:
        raise HTTPException(403, "Invalid setup code")

    info = available_devices[node_id]
    commissioned_devices[node_id] = {
        "type": info["type"],
        "name": info["name"],
        "state": _default_state_for_type(info["type"]),
    }
    return {
        "status": "commissioned",
        "node_id": node_id,
        "name": info["name"],
        "type": info["type"],
    }


@app.delete("/api/devices/{node_id}/commission")
async def decommission_device(node_id: int):
    """Descomisiona un dispositivo (vuelve a estado disponible)."""
    if node_id not in commissioned_devices:
        raise HTTPException(404, f"Device {node_id} is not commissioned")

    del commissioned_devices[node_id]
    return {"status": "decommissioned", "node_id": node_id}


# --- Endpoints de dispositivos comisionados ---


@app.get("/api/devices")
async def list_devices():
    """Lista todos los dispositivos comisionados con su estado actual."""
    devices = []
    for node_id, info in commissioned_devices.items():
        devices.append({
            "node_id": node_id,
            "type": info["type"],
            "name": info.get("name", f"Device {node_id}"),
            "state": info["state"],
        })
    return {"devices": devices}


@app.get("/api/devices/{node_id}")
async def get_device(node_id: int):
    """Estado detallado de un dispositivo comisionado."""
    if node_id not in commissioned_devices:
        raise HTTPException(404, f"Device {node_id} not found (not commissioned?)")

    info = commissioned_devices[node_id]
    return {
        "node_id": node_id,
        "type": info["type"],
        "name": info.get("name", f"Device {node_id}"),
        "state": info["state"],
    }


@app.post("/api/devices/{node_id}/command")
async def send_command(node_id: int, request: CommandRequest):
    """Envía un comando a un dispositivo comisionado."""
    if node_id not in commissioned_devices:
        raise HTTPException(404, f"Device {node_id} not found (not commissioned?)")

    device = commissioned_devices[node_id]
    result = _handle_command(device, request.command, request.args)
    return {"node_id": node_id, "command": request.command, "result": result}


@app.post("/api/devices/{node_id}/trigger")
async def trigger_event(node_id: int, request: TriggerRequest):
    """Dispara un evento en un dispositivo (alarma, fuga, contacto)."""
    if node_id not in commissioned_devices:
        raise HTTPException(404, f"Device {node_id} not found (not commissioned?)")

    device = commissioned_devices[node_id]
    result = _handle_trigger(device, request.event_type)

    event = {
        "node_id": node_id,
        "event_type": request.event_type,
        "timestamp": datetime.now().isoformat(),
        "result": result,
    }
    event_history.append(event)
    return event


@app.get("/api/devices/{node_id}/events")
async def get_device_events(node_id: int):
    """Historial de eventos de un dispositivo."""
    if node_id not in commissioned_devices:
        raise HTTPException(404, f"Device {node_id} not found (not commissioned?)")

    events = [e for e in event_history if e["node_id"] == node_id]
    return {"node_id": node_id, "events": events}


# --- Lógica mock ---


def _default_state_for_type(device_type: str) -> dict[str, Any]:
    """Estado inicial por defecto para un tipo de dispositivo."""
    defaults: dict[str, dict[str, Any]] = {
        "lighting": {"on_off": False, "brightness": 0, "hue": 0, "saturation": 0},
        "switch": {"on_off": False},
        "lock": {"lock_state": "locked"},
        "contact_sensor": {"contact": True},  # True = cerrado
        "window": {"current_position_lift_percentage": 0},
        "media_player": {"on_off": False, "playback": "stopped", "media_url": ""},
        "smoke": {"smoke_state": 0},
        "water_leak": {"leak_detected": False},
        "temperature": {"measured_value": 2150},
        "thermostat": {"local_temperature": 2100, "occupied_heating_setpoint": 2200},
    }
    return dict(defaults.get(device_type, {}))


def _handle_command(
    device: dict[str, Any], command: str, args: list[str]
) -> dict[str, Any]:
    """Procesa un comando mutando el estado en memoria."""
    state = device["state"]
    device_type = device["type"]

    match device_type:
        case "lighting":
            return _handle_lighting(state, command, args)
        case "switch":
            return _handle_switch(state, command)
        case "lock":
            return _handle_lock(state, command)
        case "contact_sensor":
            return _handle_contact_sensor(state, command)
        case "window":
            return _handle_window(state, command, args)
        case "media_player":
            return _handle_media_player(state, command, args)
        case "smoke":
            return _handle_smoke(state, command)
        case "water_leak":
            return _handle_water_leak(state, command)
        case "temperature":
            return _handle_temperature(state, command)
        case "thermostat":
            return _handle_thermostat(state, command, args)
        case _:
            return _error(f"unknown device type: {device_type}")


def _handle_lighting(
    state: dict, command: str, args: list[str]
) -> dict[str, Any]:
    match command:
        case "on":
            state["on_off"] = True
            if state["brightness"] == 0:
                state["brightness"] = 254
            return _ok("light turned on")
        case "off":
            state["on_off"] = False
            return _ok("light turned off")
        case "brightness":
            level = int(args[0]) if args else 127
            state["brightness"] = max(0, min(254, level))
            state["on_off"] = level > 0
            return _ok(f"brightness set to {level}")
        case "color":
            hue = int(args[0]) if len(args) > 0 else 0
            sat = int(args[1]) if len(args) > 1 else 0
            state["hue"] = max(0, min(254, hue))
            state["saturation"] = max(0, min(254, sat))
            return _ok(f"color set to hue={hue} sat={sat}")
        case "status":
            return {"status": "ok", **state}
        case _:
            return _error(f"unknown lighting action: {command}")


def _handle_switch(state: dict, command: str) -> dict[str, Any]:
    match command:
        case "on":
            state["on_off"] = True
            return _ok("switch turned on")
        case "off":
            state["on_off"] = False
            return _ok("switch turned off")
        case "toggle":
            state["on_off"] = not state["on_off"]
            status = "on" if state["on_off"] else "off"
            return _ok(f"switch toggled to {status}")
        case "status":
            return {"status": "ok", **state}
        case _:
            return _error(f"unknown switch action: {command}")


def _handle_lock(state: dict, command: str) -> dict[str, Any]:
    match command:
        case "lock":
            state["lock_state"] = "locked"
            return _ok("door locked")
        case "unlock":
            state["lock_state"] = "unlocked"
            return _ok("door unlocked")
        case "status":
            return {"status": "ok", **state}
        case _:
            return _error(f"unknown lock action: {command}")


def _handle_contact_sensor(state: dict, command: str) -> dict[str, Any]:
    match command:
        case "open":
            state["contact"] = False
            return _ok("contact sensor: open")
        case "close":
            state["contact"] = True
            return _ok("contact sensor: closed")
        case "status":
            return {"status": "ok", **state}
        case _:
            return _error(f"unknown contact_sensor action: {command}")


def _handle_window(
    state: dict, command: str, args: list[str]
) -> dict[str, Any]:
    match command:
        case "set":
            percent = int(args[0]) if args else 0
            state["current_position_lift_percentage"] = max(0, min(100, percent))
            return _ok(f"window set to {percent}%")
        case "open":
            state["current_position_lift_percentage"] = 100
            return _ok("window fully opened")
        case "close":
            state["current_position_lift_percentage"] = 0
            return _ok("window fully closed")
        case "status":
            return {"status": "ok", **state}
        case _:
            return _error(f"unknown window action: {command}")


def _handle_media_player(
    state: dict, command: str, args: list[str]
) -> dict[str, Any]:
    match command:
        case "on":
            state["on_off"] = True
            return _ok("TV turned on")
        case "off":
            state["on_off"] = False
            state["playback"] = "stopped"
            state["media_url"] = ""
            return _ok("TV turned off")
        case "play":
            url = args[0] if args else ""
            state["on_off"] = True
            state["playback"] = "playing"
            state["media_url"] = url
            return _ok(f"playing {url}" if url else "playback resumed")
        case "pause":
            state["playback"] = "paused"
            return _ok("playback paused")
        case "stop":
            state["playback"] = "stopped"
            state["media_url"] = ""
            return _ok("playback stopped")
        case "status":
            return {"status": "ok", **state}
        case _:
            return _error(f"unknown media_player action: {command}")


def _handle_smoke(state: dict, command: str) -> dict[str, Any]:
    match command:
        case "status":
            return {"status": "ok", **state}
        case _:
            return {"status": "ok", "smoke_state": state["smoke_state"]}


def _handle_water_leak(state: dict, command: str) -> dict[str, Any]:
    match command:
        case "status":
            return {"status": "ok", **state}
        case _:
            return {"status": "ok", "leak_detected": state["leak_detected"]}


def _handle_temperature(state: dict, command: str) -> dict[str, Any]:
    drift = random.randint(-50, 50)
    state["measured_value"] += drift
    return {"status": "ok", "measured_value": state["measured_value"]}


def _handle_thermostat(
    state: dict, command: str, args: list[str]
) -> dict[str, Any]:
    match command:
        case "read":
            return {"status": "ok", **state}
        case "set":
            temp = int(args[0]) if args else 2200
            state["occupied_heating_setpoint"] = temp
            return _ok(f"setpoint set to {temp}")
        case _:
            return _error(f"unknown thermostat action: {command}")


def _handle_trigger(
    device: dict[str, Any], event_type: str
) -> dict[str, Any]:
    """Procesa un trigger de evento."""
    state = device["state"]
    match event_type:
        case "smoke-alarm":
            state["smoke_state"] = 1
            return _ok("smoke alarm triggered")
        case "smoke-clear":
            state["smoke_state"] = 0
            return _ok("smoke alarm cleared")
        case "water-leak":
            state["leak_detected"] = True
            return _ok("water leak detected")
        case "water-clear":
            state["leak_detected"] = False
            return _ok("water leak cleared")
        case "contact-open":
            state["contact"] = False
            return _ok("contact opened")
        case "contact-close":
            state["contact"] = True
            return _ok("contact closed")
        case _:
            return _error(f"unknown trigger: {event_type}")


def _ok(message: str) -> dict[str, str]:
    return {"status": "ok", "message": message}


def _error(message: str) -> dict[str, str]:
    return {"status": "error", "message": message}


# --- Frontend estático ---

FRONTEND_DIR = Path(__file__).resolve().parent.parent / "frontend"
if FRONTEND_DIR.exists():
    app.mount("/", StaticFiles(directory=str(FRONTEND_DIR), html=True), name="frontend")
