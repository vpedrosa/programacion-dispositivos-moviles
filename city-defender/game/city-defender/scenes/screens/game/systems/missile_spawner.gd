class_name MissileSpawner
extends Node

const POOL_INITIAL_SIZE: int = 15

@export var normal_missile_scene: PackedScene
@export var fast_missile_scene: PackedScene
@export var heavy_missile_scene: PackedScene

@export var difficulty_manager: DifficultyManager
@export var wave_size: int = 5

var _spawn_timer: float = 0.0
var _pools: Dictionary = {}
var _wave_pending: bool = false


func _ready() -> void:
	_init_pools.call_deferred()


func mark_wave_pending(_wave_number: int) -> void:
	_wave_pending = true


func _init_pools() -> void:
	for scene: PackedScene in [normal_missile_scene, fast_missile_scene, heavy_missile_scene]:
		if scene == null:
			continue
		_pools[scene] = []
		for i in POOL_INITIAL_SIZE:
			_add_to_pool(scene)


func _process(delta: float) -> void:
	if difficulty_manager == null:
		return
	_spawn_timer += delta
	if _spawn_timer >= difficulty_manager.spawn_interval:
		_spawn_timer = 0.0
		if _wave_pending:
			_wave_pending = false
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
	var missile: EnemyMissile = _acquire(scene)
	missile.speed = difficulty_manager.missile_speed
	var spawn_x := randf_range(50.0, get_viewport().get_visible_rect().size.x - 50.0)
	missile.init(Vector2(spawn_x, -20.0), target)


func _spawn_wave() -> void:
	for _i in range(wave_size):
		_spawn_missile()


func _acquire(scene: PackedScene) -> EnemyMissile:
	for missile: EnemyMissile in _pools.get(scene, []):
		if not missile.visible:
			return missile
	return _add_to_pool(scene)


func _add_to_pool(scene: PackedScene) -> EnemyMissile:
	var missile: EnemyMissile = scene.instantiate()
	missile.visible = false
	missile.set_process(false)
	get_parent().add_child(missile)
	missile.deactivate()
	if not _pools.has(scene):
		_pools[scene] = []
	_pools[scene].append(missile)
	return missile


func _scene_for_type(type: int) -> PackedScene:
	match type:
		MissileType.FAST:  return fast_missile_scene
		MissileType.HEAVY: return heavy_missile_scene
		_:                 return normal_missile_scene


func _get_random_alive_city() -> City:
	var alive: Array[City] = []
	for c: City in get_tree().get_nodes_in_group("cities"):
		if c.is_alive:
			alive.append(c)
	if alive.is_empty():
		return null
	return alive[randi() % alive.size()]
