class_name EnemyMissile
extends Area2D

const IMPACT_SCENE = preload("res://scenes/entities/explosion/impact_explosion.tscn")

signal missile_destroyed(missile: EnemyMissile)

@export var speed: float = 150.0
@export var score_value: int = 100
@export var money_value: int = 15
@export var max_hits: int = 1

@onready var _visual: Sprite2D = $Visual

var _hits: int = 0
var _direction: Vector2
var _target_city: Node2D
var _smoke: CPUParticles2D


func _ready() -> void:
	collision_layer = 4   # enemy_missiles layer
	collision_mask = 1    # detect cities layer
	var shape := CircleShape2D.new()
	shape.radius = 6.0
	$CollisionShape2D.shape = shape
	area_entered.connect(_on_area_entered)
	_setup_glow()
	_setup_smoke()


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
		var impact: Node2D = IMPACT_SCENE.instantiate()
		get_parent().call_deferred("add_child", impact)
		impact.global_position = global_position
		area.take_damage()
		AudioManager.play_sfx("missile-collision")
		call_deferred("queue_free")


# ── Efectos visuales ──────────────────────────────────────────────────────────

func _setup_glow() -> void:
	# Punto radial generado por código: gradiente blanco→transparente
	var size := 32
	var center := size / 2.0
	var img: Image = Image.create(size, size, false, Image.FORMAT_RGBA8)
	for x in size:
		for y in size:
			var d: float = Vector2(x, y).distance_to(Vector2(center, center)) / center
			var a: float = clamp(1.0 - d, 0.0, 1.0)
			a = pow(a, 1.5)
			img.set_pixel(x, y, Color(1.0, 1.0, 1.0, a))
	var tex: ImageTexture = ImageTexture.create_from_image(img)

	var glow := Sprite2D.new()
	glow.texture = tex
	glow.position = Vector2(0.0, 6.0)  # local +Y = escape/cola del cohete
	glow.scale = Vector2(0.55, 0.55)
	glow.modulate = Color(_visual.modulate.r, _visual.modulate.g, _visual.modulate.b, 0.9)
	var mat := CanvasItemMaterial.new()
	mat.blend_mode = CanvasItemMaterial.BLEND_MODE_ADD
	glow.material = mat
	add_child(glow)
	move_child(glow, _visual.get_index())  # justo antes de Visual → detrás


func _setup_smoke() -> void:
	_smoke = CPUParticles2D.new()
	_smoke.emitting = true
	_smoke.amount = 10
	_smoke.lifetime = 0.22
	_smoke.explosiveness = 0.0
	_smoke.randomness = 0.6
	# Local (0, +Y) = detrás del misil gracias a la rotación del nodo padre
	_smoke.position = Vector2(0.0, 8.0)
	_smoke.direction = Vector2(0.0, 1.0)
	_smoke.spread = 35.0
	_smoke.initial_velocity_min = 5.0
	_smoke.initial_velocity_max = 18.0
	_smoke.gravity = Vector2.ZERO
	_smoke.scale_amount_min = 3.0
	_smoke.scale_amount_max = 7.0
	_smoke.color = Color(1.0, 1.0, 1.0, 1.0)  # el ramp controla color y alpha

	# Curva exponencial: partículas grandes al nacer (cerca del cohete)
	# y decaen rápidamente al alejarse
	var scale_curve := Curve.new()
	scale_curve.add_point(Vector2(0.0, 1.0), 0.0, -4.0)
	scale_curve.add_point(Vector2(1.0, 0.0), -0.3, 0.0)
	_smoke.scale_amount_curve = scale_curve

	# Fade de alpha: opaco al nacer → transparente al morir
	var ramp := Gradient.new()
	ramp.set_color(0, Color(0.75, 0.75, 0.75, 0.85))
	ramp.set_color(1, Color(0.75, 0.75, 0.75, 0.0))
	_smoke.color_ramp = ramp

	add_child(_smoke)
	move_child(_smoke, _visual.get_index())  # justo antes de Visual → detrás
