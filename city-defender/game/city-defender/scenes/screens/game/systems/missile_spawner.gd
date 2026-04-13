class_name MissileSpawner
extends Node

@export var normal_missile_scene: PackedScene
@export var fast_missile_scene: PackedScene
@export var heavy_missile_scene: PackedScene
@export var difficulty_manager: DifficultyManager

var _spawn_timer: float = 0.0


func _process(delta: float) -> void:
	if difficulty_manager == null:
		return
	_spawn_timer += delta
	if _spawn_timer >= difficulty_manager.spawn_interval:
		_spawn_timer = 0.0
		if difficulty_manager.is_wave_time():
			_spawn_wave()
		else:
			_spawn_missile()


func _spawn_missile() -> void:
	var target := _get_random_alive_city()
	if target == null:
		return
	var scene := _scene_for_type(difficulty_manager.get_missile_type())
	if scene == null:
		return
	var missile: EnemyMissile = scene.instantiate()
	get_parent().add_child(missile)
	var spawn_x := randf_range(50.0, get_viewport().get_visible_rect().size.x - 50.0)
	missile.speed = difficulty_manager.missile_speed
	missile.init(Vector2(spawn_x, -20.0), target)


func _spawn_wave() -> void:
	for _i in range(5):
		_spawn_missile()


func _scene_for_type(type: String) -> PackedScene:
	match type:
		"fast":  return fast_missile_scene
		"heavy": return heavy_missile_scene
		_:       return normal_missile_scene


func _get_random_alive_city() -> City:
	var alive: Array[City] = []
	for c: City in get_tree().get_nodes_in_group("cities"):
		if c.is_alive:
			alive.append(c)
	if alive.is_empty():
		return null
	return alive[randi() % alive.size()]
