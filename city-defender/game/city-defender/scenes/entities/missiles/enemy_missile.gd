class_name EnemyMissile
extends Area2D

signal missile_destroyed(missile: EnemyMissile)

@export var speed: float = 150.0
@export var score_value: int = 100
@export var money_value: int = 10
@export var max_hits: int = 1

var _hits: int = 0
var _direction: Vector2
var _target_city: Node2D


func _ready() -> void:
	collision_layer = 4   # enemy_missiles layer
	collision_mask = 1    # detect cities layer
	var shape := CircleShape2D.new()
	shape.radius = 6.0
	$CollisionShape2D.shape = shape
	area_entered.connect(_on_area_entered)


func init(from: Vector2, target_city: Node2D) -> void:
	global_position = from
	_target_city = target_city
	if target_city:
		_direction = (target_city.global_position - from).normalized()
		_update_rotation()
	add_to_group("enemy_missiles")


func _process(delta: float) -> void:
	global_position += _direction * speed * delta
	_check_out_of_bounds()


func hit() -> void:
	_hits += 1
	if _hits >= max_hits:
		_on_destroyed()
	else:
		_on_hit_survived()


func _on_hit_survived() -> void:
	# Override in subclasses for visual feedback
	modulate = Color(1.0, 0.6, 0.4)


func _on_destroyed() -> void:
	GameState.add_score(score_value)
	GameState.add_money(money_value)
	missile_destroyed.emit(self)
	queue_free()


func _update_rotation() -> void:
	if _direction != Vector2.ZERO:
		rotation = _direction.angle() + PI / 2.0


func _check_out_of_bounds() -> void:
	var viewport_size := get_viewport_rect().size
	if global_position.y > viewport_size.y + 50:
		queue_free()


func _on_area_entered(area: Area2D) -> void:
	if area is City and area.is_alive:
		area.take_damage()
		queue_free()
