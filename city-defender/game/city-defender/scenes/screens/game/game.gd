extends Node2D

@onready var difficulty_manager: Node = $DifficultyManager
@onready var missile_spawner: Node = $MissileSpawner
@onready var powerup_manager: Node = $PowerupManager
@onready var defense_base: Node2D = $DefenseBase
@onready var hud: CanvasLayer = $HUD
@onready var shop: CanvasLayer = $Shop

var _cities: Array[Node] = []


func _ready() -> void:
	GameState.reset()
	GameState.game_over.connect(_on_game_over)
	_cities = get_tree().get_nodes_in_group("cities")
	hud.shop_requested.connect(_on_shop_requested)
	hud.emp_activated.connect(_on_emp_activated)
	shop.closed.connect(_on_shop_closed)
	shop.powerup_purchased.connect(_on_powerup_purchased)


func _input(event: InputEvent) -> void:
	if event is InputEventScreenTouch and event.pressed:
		defense_base.shoot_at(event.position)


func get_cities() -> Array[Node]:
	return _cities


func _on_game_over() -> void:
	get_tree().change_scene_to_file("res://scenes/screens/game_over/game_over.tscn")


func _on_shop_requested() -> void:
	shop.open(_cities)


func _on_shop_closed() -> void:
	pass


func _on_powerup_purchased(powerup_id: String) -> void:
	powerup_manager.apply_powerup(powerup_id, _cities)


func _on_emp_activated() -> void:
	powerup_manager.apply_powerup("emp", _cities)
