"""Backend API REST para control de dispositivos Matter simulados."""

import asyncio
import json
import os
import subprocess
from datetime import datetime
from pathlib import Path
from typing import Any

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel

app = FastAPI(title="Smart Home Matter Simulation API", version="1.0.0")

# Frontend estático (se monta al final del archivo para no interferir con /api)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# Rutas
SIMULATION_DIR = Path(__file__).resolve().parent.parent.parent
SCRIPTS_DIR = SIMULATION_DIR / "scripts"
CONTROL_SCRIPT = SCRIPTS_DIR / "control.sh"
LOG_DIR = SIMULATION_DIR / "logs"

# Estado de dispositivos comisionados (en memoria)
commissioned_devices: dict[int, dict[str, Any]] = {}

# Historial de eventos
event_history: list[dict[str, Any]] = []


class CommandRequest(BaseModel):
    cluster: str
    command: str
    args: list[str] = []


class TriggerRequest(BaseModel):
    event_type: str  # "smoke-alarm", "water-leak"


# --- Dispositivos ---


@app.get("/api/devices")
async def list_devices():
    """Lista todos los dispositivos comisionados con su tipo y estado."""
    devices = []
    for node_id, info in commissioned_devices.items():
        state = await _read_device_state(node_id, info["type"])
        devices.append({
            "node_id": node_id,
            "type": info["type"],
            "name": info.get("name", f"Device {node_id}"),
            "port": info.get("port"),
            "state": state,
        })
    return {"devices": devices}


@app.get("/api/devices/{node_id}")
async def get_device(node_id: int):
    """Estado detallado de un dispositivo."""
    if node_id not in commissioned_devices:
        raise HTTPException(404, f"Device {node_id} not found")

    info = commissioned_devices[node_id]
    state = await _read_device_state(node_id, info["type"])
    return {
        "node_id": node_id,
        "type": info["type"],
        "name": info.get("name", f"Device {node_id}"),
        "port": info.get("port"),
        "state": state,
    }


@app.post("/api/devices/{node_id}/command")
async def send_command(node_id: int, request: CommandRequest):
    """Envía un comando a un dispositivo."""
    if node_id not in commissioned_devices:
        raise HTTPException(404, f"Device {node_id} not found")

    device_type = commissioned_devices[node_id]["type"]
    args = [str(CONTROL_SCRIPT), device_type, request.command, str(node_id)]
    args.extend(request.args)

    result = await _run_script(args)
    return {"node_id": node_id, "command": request.command, "result": result}


@app.post("/api/devices/{node_id}/trigger")
async def trigger_event(node_id: int, request: TriggerRequest):
    """Dispara un evento en un dispositivo (ej: alarma de humo)."""
    if node_id not in commissioned_devices:
        raise HTTPException(404, f"Device {node_id} not found")

    args = [str(CONTROL_SCRIPT), "trigger", request.event_type, str(node_id)]
    result = await _run_script(args)

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
        raise HTTPException(404, f"Device {node_id} not found")

    events = [e for e in event_history if e["node_id"] == node_id]
    return {"node_id": node_id, "events": events}


# --- Gestión de dispositivos ---


@app.post("/api/devices/register")
async def register_device(
    node_id: int,
    device_type: str,
    name: str = "",
    port: int = 5540,
):
    """Registra un dispositivo comisionado manualmente."""
    commissioned_devices[node_id] = {
        "type": device_type,
        "name": name or f"{device_type.capitalize()} {node_id}",
        "port": port,
    }
    return {"status": "registered", "node_id": node_id}


# --- Helpers ---


async def _run_script(args: list[str]) -> dict[str, Any]:
    """Ejecuta un script y parsea la salida JSON."""
    try:
        proc = await asyncio.create_subprocess_exec(
            *args,
            stdout=asyncio.subprocess.PIPE,
            stderr=asyncio.subprocess.PIPE,
        )
        stdout, stderr = await asyncio.wait_for(proc.communicate(), timeout=30)
        output = stdout.decode().strip()
        try:
            return json.loads(output)
        except json.JSONDecodeError:
            return {"raw_output": output, "stderr": stderr.decode().strip()}
    except asyncio.TimeoutError:
        return {"error": "command timed out"}
    except Exception as e:
        return {"error": str(e)}


async def _read_device_state(node_id: int, device_type: str) -> dict[str, Any]:
    """Lee el estado actual de un dispositivo via chip-tool."""
    state: dict[str, Any] = {}
    match device_type:
        case "lighting":
            on_off = await _run_script(
                [str(CONTROL_SCRIPT), "lighting", "status", str(node_id)]
            )
            state["on_off"] = on_off
        case "lock":
            lock_state = await _run_script(
                [str(CONTROL_SCRIPT), "lock", "status", str(node_id)]
            )
            state["lock_state"] = lock_state
        case "thermostat":
            therm = await _run_script(
                [str(CONTROL_SCRIPT), "thermostat", "read", str(node_id)]
            )
            state["thermostat"] = therm
        case "window":
            pos = await _run_script(
                [str(CONTROL_SCRIPT), "window", "status", str(node_id)]
            )
            state["position"] = pos
        case "sensor" | "smoke" | "temperature":
            state["type"] = device_type
    return state


# Montar frontend estático al final para no interferir con /api
FRONTEND_DIR = Path(__file__).resolve().parent.parent / "frontend"
if FRONTEND_DIR.exists():
    app.mount("/", StaticFiles(directory=str(FRONTEND_DIR), html=True), name="frontend")
