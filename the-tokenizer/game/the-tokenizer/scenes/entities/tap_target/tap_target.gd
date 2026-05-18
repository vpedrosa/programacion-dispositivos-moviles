extends Control

## Mecánica de tap manual de Era 1.
##
## La escena se instancia dentro del PlayArea del HUD por game.gd. Cada
## pulsación suma tokens_per_tap al balance vía GameState.add_tokens y, si
## el modo evaluación está activo, añade un bono extra al balance gastable
## que no toca lifetime/era_lifetime (de modo que boss y eventos no se
## aceleren). Reproduce SFX, anima un bounce sobre la propia escena y
## dispara un "+N" flotante con la cantidad total añadida.

const FLOAT_DURATION := 0.7
const FLOAT_DISTANCE := 90.0
const BOUNCE_SCALE := 1.08

@onready var _button: TextureButton = %TapButton
@onready var _float_layer: Control = %FloatLayer

var _bounce_tween: Tween


func _ready() -> void:
	pivot_offset = size * 0.5
	_button.pressed.connect(_on_tap)


func _on_tap() -> void:
	if SceneManager.has_overlay():
		return
	var base := GameState.state.tokens_per_tap
	GameState.add_tokens(base)
	var bonus := DebugFlags.bonus_for(base)
	GameState.add_debug_bonus(bonus)
	AudioManager.play_button_sfx()
	_play_bounce()
	_spawn_floating(base + bonus)


func _play_bounce() -> void:
	if _bounce_tween and _bounce_tween.is_running():
		_bounce_tween.kill()
	scale = Vector2(BOUNCE_SCALE, BOUNCE_SCALE)
	_bounce_tween = create_tween()
	_bounce_tween.tween_property(self, "scale", Vector2.ONE, 0.15).set_trans(Tween.TRANS_BACK).set_ease(Tween.EASE_OUT)


func _spawn_floating(amount: float) -> void:
	var label := Label.new()
	label.text = "+%s" % _format(amount)
	label.add_theme_font_size_override("font_size", 28)
	label.add_theme_color_override("font_color", Color(0.92, 0.96, 0.85, 1))
	label.add_theme_constant_override("outline_size", 4)
	label.add_theme_color_override("font_outline_color", Color(0, 0.1, 0, 0.8))
	label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	label.mouse_filter = Control.MOUSE_FILTER_IGNORE
	_float_layer.add_child(label)
	var rect := _button.get_global_rect()
	var local_centre := _float_layer.get_global_transform().affine_inverse() * rect.get_center()
	label.position = local_centre - label.size * 0.5 + Vector2(randf_range(-30.0, 30.0), -20.0)
	var t := create_tween()
	t.set_parallel()
	t.tween_property(label, "position:y", label.position.y - FLOAT_DISTANCE, FLOAT_DURATION).set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_OUT)
	t.tween_property(label, "modulate:a", 0.0, FLOAT_DURATION).set_delay(0.15)
	t.chain().tween_callback(label.queue_free)


static func _format(value: float) -> String:
	if value >= 1_000.0:
		return "%.1fk" % (value / 1_000.0)
	if value >= 100.0:
		return "%d" % roundi(value)
	return "%.1f" % value
