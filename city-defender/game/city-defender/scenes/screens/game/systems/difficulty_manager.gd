class_name DifficultyManager
extends Node

signal wave_started(wave_number: int)

## Fases de dificultad según el tiempo transcurrido.
enum DifficultyPhase { EARLY, MID, LATE }

@export var initial_spawn_interval: float = 2.0
@export var min_spawn_interval: float = 0.4
@export var initial_speed: float = 150.0
@export var max_speed: float = 400.0
@export var fast_missile_unlock_time: float = 30.0
@export var heavy_missile_unlock_time: float = 90.0
@export var wave_interval: float = 20.0

var elapsed_time: float = 0.0
var spawn_interval: float
var missile_speed: float

var _phase: DifficultyPhase = DifficultyPhase.EARLY
var _last_wave: int = 0


func _ready() -> void:
	spawn_interval = initial_spawn_interval
	missile_speed = initial_speed


func _process(delta: float) -> void:
	elapsed_time += delta
	_update_difficulty()
	_update_phase()
	var current_wave := int(elapsed_time / wave_interval)
	if current_wave > _last_wave and elapsed_time >= wave_interval:
		_last_wave = current_wave
		wave_started.emit(current_wave)


func _update_difficulty() -> void:
	var t := clampf(elapsed_time / 120.0, 0.0, 1.0)
	spawn_interval = lerpf(initial_spawn_interval, min_spawn_interval, t)
	missile_speed = lerpf(initial_speed, max_speed, t)


func _update_phase() -> void:
	if elapsed_time < fast_missile_unlock_time:
		_phase = DifficultyPhase.EARLY
	elif elapsed_time < heavy_missile_unlock_time:
		_phase = DifficultyPhase.MID
	else:
		_phase = DifficultyPhase.LATE


func get_missile_type() -> String:
	match _phase:
		DifficultyPhase.EARLY:
			return "normal"
		DifficultyPhase.MID:
			return "normal" if randf() < 0.6 else "fast"
		DifficultyPhase.LATE:
			var r := randf()
			if r < 0.4:
				return "normal"
			elif r < 0.7:
				return "fast"
			else:
				return "heavy"
	return "normal"


func reset() -> void:
	elapsed_time = 0.0
	_last_wave = 0
	_phase = DifficultyPhase.EARLY
	spawn_interval = initial_spawn_interval
	missile_speed = initial_speed


func is_wave_time() -> bool:
	if elapsed_time < wave_interval:
		return false
	var prev := elapsed_time - get_process_delta_time()
	return int(prev / wave_interval) < int(elapsed_time / wave_interval)
