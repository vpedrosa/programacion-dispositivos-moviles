class_name DifficultyManager
extends Node

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


func _ready() -> void:
	spawn_interval = initial_spawn_interval
	missile_speed = initial_speed


func _process(delta: float) -> void:
	elapsed_time += delta
	_update_difficulty()


func _update_difficulty() -> void:
	var t := clampf(elapsed_time / 120.0, 0.0, 1.0)
	spawn_interval = lerpf(initial_spawn_interval, min_spawn_interval, t)
	missile_speed = lerpf(initial_speed, max_speed, t)


func get_missile_type() -> String:
	if elapsed_time < fast_missile_unlock_time:
		return "normal"
	elif elapsed_time < heavy_missile_unlock_time:
		return "normal" if randf() < 0.6 else "fast"
	else:
		var r := randf()
		if r < 0.4:
			return "normal"
		elif r < 0.7:
			return "fast"
		else:
			return "heavy"


func is_wave_time() -> bool:
	if elapsed_time < wave_interval:
		return false
	var prev := elapsed_time - get_process_delta_time()
	return int(prev / wave_interval) < int(elapsed_time / wave_interval)
