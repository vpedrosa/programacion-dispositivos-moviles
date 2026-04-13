class_name EnemyMissileFast
extends EnemyMissile

const CURVE_AMPLITUDE: float = 60.0   # píxeles de desviación máxima
const CURVE_FREQUENCY: float = 0.6    # oscilaciones por segundo

var _elapsed: float = 0.0
var _spawn_pos: Vector2


func _ready() -> void:
	super._ready()
	speed = 300.0
	score_value = 200
	money_value = 30
	max_hits = 1


func init(from: Vector2, target_city: Node2D) -> void:
	super.init(from, target_city)
	_spawn_pos = from


func _process(delta: float) -> void:
	if _direction == Vector2.ZERO:
		return
	_elapsed += delta

	var perp := Vector2(-_direction.y, _direction.x)

	# Posición en la curva: línea recta + desplazamiento sinusoidal perpendicular
	global_position = _spawn_pos \
		+ _direction * speed * _elapsed \
		+ perp * sin(_elapsed * CURVE_FREQUENCY * TAU) * CURVE_AMPLITUDE

	# Rotación siguiendo la tangente de la curva para aspecto realista
	var tangent := _direction * speed \
		+ perp * cos(_elapsed * CURVE_FREQUENCY * TAU) * CURVE_AMPLITUDE * CURVE_FREQUENCY * TAU
	if tangent != Vector2.ZERO:
		rotation = tangent.angle() + PI / 2.0

	_check_out_of_bounds()
