extends Node2D

@onready var difficulty_manager: DifficultyManager = $DifficultyManager
@onready var missile_spawner: MissileSpawner = $MissileSpawner
@onready var powerup_manager: PowerupManager = $PowerupManager
@onready var defense_base: DefenseBase = $DefenseBase
@onready var hud: CanvasLayer = $HUD if has_node("HUD") else null
@onready var shop: CanvasLayer = $Shop if has_node("Shop") else null

var _cities: Array = []


func _ready() -> void:
	GameState.reset()
	GameState.game_over.connect(_on_game_over)
	AudioManager.play_music("main-theme")
	_add_scanline_overlay()
	_cities = get_tree().get_nodes_in_group("cities")
	if hud:
		hud.shop_requested.connect(_on_shop_requested)
		hud.emp_activated.connect(_on_emp_activated)
		_connect_city_health_to_hud()
	if shop:
		shop.closed.connect(_on_shop_closed)
		shop.powerup_purchased.connect(_on_powerup_purchased)


func _process(_delta: float) -> void:
	if hud and defense_base:
		hud.update_cooldown(defense_base.get_cooldown_progress())


func _input(event: InputEvent) -> void:
	if event is InputEventScreenTouch and event.pressed:
		defense_base.shoot_at(event.position)
	elif event is InputEventMouseButton and event.pressed:
		# Editor/desktop fallback
		defense_base.shoot_at(event.position)


func get_cities() -> Array:
	return _cities


func _connect_city_health_to_hud() -> void:
	for city in _cities:
		var c := city as City
		c.health_changed.connect(
			func(h: int, m: int) -> void: hud.update_city_health(c.city_index, h, m)
		)


func _on_game_over() -> void:
	get_tree().call_deferred("change_scene_to_file", "res://scenes/screens/game_over/game_over.tscn")


func _on_shop_requested() -> void:
	if shop:
		shop.open(_cities)


func _on_shop_closed() -> void:
	pass


func _on_powerup_purchased(powerup_id: String) -> void:
	powerup_manager.apply_powerup(powerup_id, _cities)


func _on_emp_activated() -> void:
	powerup_manager.apply_powerup("emp", _cities)


func _add_scanline_overlay() -> void:
	var layer := CanvasLayer.new()
	layer.layer = 10
	add_child(layer)
	var rect := ColorRect.new()
	rect.mouse_filter = Control.MOUSE_FILTER_IGNORE
	rect.color = Color(0, 0, 0, 0)
	rect.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	var mat := ShaderMaterial.new()
	mat.shader = load("res://assets/shaders/scanlines.gdshader")
	rect.material = mat
	layer.add_child(rect)
