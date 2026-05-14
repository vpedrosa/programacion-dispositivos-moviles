class_name PlayerState
extends Resource

## Estado serializable de una partida.
##
## Solo datos crudos + helpers de (de)serialización. La lógica de juego
## (compra de mejoras, aplicación de multiplicadores, etc.) vive en los
## servicios que consumen este estado.

const ERA_BASEMENT := 1
const ERA_SINGULARITY := 7

@export var tokens: float = 0.0
@export var tokens_per_tap: float = 1.0
@export var tokens_per_second: float = 0.0
@export var qubits: int = 0
@export var qubit_multiplier: float = 1.0
@export var current_era: int = ERA_BASEMENT
@export var boss_progress: float = 0.0
@export var upgrade_levels: Dictionary = {}
@export var ethical_decisions: Dictionary = {}
@export var triggered_events: Array[StringName] = []


static func new_default() -> PlayerState:
	return PlayerState.new()


func to_dict() -> Dictionary:
	return {
		"tokens": tokens,
		"tokens_per_tap": tokens_per_tap,
		"tokens_per_second": tokens_per_second,
		"qubits": qubits,
		"qubit_multiplier": qubit_multiplier,
		"current_era": current_era,
		"boss_progress": boss_progress,
		"upgrade_levels": upgrade_levels,
		"ethical_decisions": ethical_decisions,
		"triggered_events": triggered_events.map(func(e: StringName) -> String: return String(e)),
	}


static func from_dict(data: Dictionary) -> PlayerState:
	var state := PlayerState.new()
	state.tokens = float(data.get("tokens", 0.0))
	state.tokens_per_tap = float(data.get("tokens_per_tap", 1.0))
	state.tokens_per_second = float(data.get("tokens_per_second", 0.0))
	state.qubits = int(data.get("qubits", 0))
	state.qubit_multiplier = float(data.get("qubit_multiplier", 1.0))
	state.current_era = int(data.get("current_era", ERA_BASEMENT))
	state.boss_progress = clampf(float(data.get("boss_progress", 0.0)), 0.0, 1.0)
	if data.has("upgrade_levels"):
		var raw: Dictionary = data.get("upgrade_levels", {})
		state.upgrade_levels = {}
		for key in raw.keys():
			state.upgrade_levels[String(key)] = int(raw[key])
	elif data.has("purchased_upgrades"):
		# Migración de saves anteriores a #300: cada id pasa a nivel 1.
		var legacy: Array = data.get("purchased_upgrades", [])
		state.upgrade_levels = {}
		for id in legacy:
			state.upgrade_levels[String(id)] = 1
	state.ethical_decisions = data.get("ethical_decisions", {}).duplicate()
	var events: Array = data.get("triggered_events", [])
	state.triggered_events.assign(events.map(func(e: Variant) -> StringName: return StringName(e)))
	return state
