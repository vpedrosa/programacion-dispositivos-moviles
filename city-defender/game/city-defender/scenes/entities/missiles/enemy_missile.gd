class_name EnemyMissile
extends Area2D

const IMPACT_SCENE = preload("res://scenes/entities/explosion/impact_explosion.tscn")

signal missile_destroyed(missile: EnemyMissile)

static var _GLOW_TEX: ImageTexture = null

@export var speed: float = 150.0
@export var score_value: int = 100
@export var money_value: int = 15
@export var max_hits: int = 1

@onready var _visual: Sprite2D = $Visual

var _hits: int = 0
var _direction: Vector2
var _target_city: Node2D
var _smoke: CPUParticles2D
var _glow: Sprite2D
var _flicker_phase: float


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
	_hits = 0
	modulate = Color.WHITE
	visible = true
	set_process(true)
	monitoring = true
	monitorable = true
	_flicker_phase = randf() * TAU
	if _smoke:
		_smoke.restart()
	global_position = from
	_target_city = target_city
	if target_city:
		_direction = (target_city.global_position - from).normalized()
		_update_rotation()
	add_to_group("enemy_missiles")


func _process(delta: float) -> void:
	global_position += _direction * speed * delta
	_check_out_of_bounds()
	if _glow:
		var t := Time.get_ticks_msec() / 1000.0 + _flicker_phase
		var f := sin(t * 11.3) * 0.18 + sin(t * 7.1) * 0.12 + sin(t * 19.7) * 0.08
		_glow.modulate.a = clamp(0.75 + f, 0.4, 1.0)
		_glow.scale = Vector2.ONE * clamp(0.55 + f * 0.5, 0.38, 0.72)


func deactivate() -> void:
	visible = false
	set_process(false)
	monitoring = false
	monitorable = false
	if is_in_group("enemy_missiles"):
		remove_from_group("enemy_missiles")
	if _smoke:
		_smoke.emitting = false


func hit() -> void:
	if not visible:
		return
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
	deactivate()


func _update_rotation() -> void:
	if _direction != Vector2.ZERO:
		rotation = _direction.angle() + PI / 2.0


func _check_out_of_bounds() -> void:
	var viewport_size := get_viewport_rect().size
	if global_position.y > viewport_size.y + 50:
		deactivate()


func _on_area_entered(area: Area2D) -> void:
	if not visible:
		return
	if area is City and area.is_alive:
		var impact: ImpactExplosion = IMPACT_SCENE.instantiate()
		get_parent().call_deferred("add_child", impact)
		impact.global_position = global_position
		area.take_damage()
		AudioManager.play_sfx("missile-collision")
		call_deferred("deactivate")


# ── Efectos visuales ──────────────────────────────────────────────────────────

static func _create_glow_texture() -> ImageTexture:
	var size := 32
	var center := size / 2.0
	var img: Image = Image.create(size, size, false, Image.FORMAT_RGBA8)
	for x in size:
		for y in size:
			var d: float = Vector2(x, y).distance_to(Vector2(center, center)) / center
			var a: float = clamp(1.0 - d, 0.0, 1.0)
			a = pow(a, 1.5)
			img.set_pixel(x, y, Color(1.0, 1.0, 1.0, a))
	return ImageTexture.create_from_image(img)


func _setup_glow() -> void:
	if _GLOW_TEX == null:
		_GLOW_TEX = _create_glow_texture()

	_flicker_phase = randf() * TAU
	_glow = Sprite2D.new()
	_glow.texture = _GLOW_TEX
	_glow.position = Vector2(0.0, 14.0)  # local +Y = escape/cola del cohete
	_glow.scale = Vector2(0.55, 0.55)
	_glow.modulate = Color(_visual.modulate.r, _visual.modulate.g, _visual.modulate.b, 0.9)
	var mat := CanvasItemMaterial.new()
	mat.blend_mode = CanvasItemMaterial.BLEND_MODE_ADD
	_glow.material = mat
	add_child(_glow)
	move_child(_glow, _visual.get_index())  # justo antes de Visual → detrás


func _setup_smoke() -> void:
	_smoke = CPUParticles2D.new()
	_smoke.emitting = true
	_smoke.amount = 10
	_smoke.lifetime = 0.65
	_smoke.explosiveness = 0.0
	_smoke.randomness = 0.6
	# Posición desplazada para dejar espacio visual entre el cohete y el humo
	_smoke.position = Vector2(0.0, 22.0)
	_smoke.direction = Vector2(0.0, 1.0)
	_smoke.spread = 35.0
	_smoke.initial_velocity_min = 8.0
	_smoke.initial_velocity_max = 22.0
	_smoke.gravity = Vector2.ZERO
	_smoke.scale_amount_min = 12.0
	_smoke.scale_amount_max = 16.0
	_smoke.color = Color(1.0, 1.0, 1.0, 1.0)  # el ramp controla color y alpha

	# Curva exponencial: partículas grandes al nacer (cerca del cohete)
	# y decaen rápidamente al alejarse
	var scale_curve := Curve.new()
	scale_curve.add_point(Vector2(0.0, 1.0), 0.0, -4.0)
	scale_curve.add_point(Vector2(1.0, 0.0), -0.3, 0.0)
	_smoke.scale_amount_curve = scale_curve

	# Fade de alpha: opaco al nacer → transparente al morir, usando el color del misil
	var mc := _visual.modulate
	var ramp := Gradient.new()
	ramp.set_color(0, Color(mc.r, mc.g, mc.b, 0.85))
	ramp.set_color(1, Color(mc.r, mc.g, mc.b, 0.0))
	_smoke.color_ramp = ramp

	add_child(_smoke)
	move_child(_smoke, _visual.get_index())  # justo antes de Visual → detrás
