extends Control

## HUD principal y contenedor de la zona de juego.
##
## Las mecánicas específicas de cada era (tap manual en Era 1, generación
## pasiva en Era 7) viven en escenas independientes que se instancian dentro
## del nodo PlayArea, mientras que el HUD que rodea esa zona se reutiliza
## entre eras.

const SHOP_SCENE := "res://scenes/screens/shop/shop.tscn"
const SETTINGS_SCENE := "res://scenes/screens/settings/settings.tscn"
const TAP_TARGET_SCENE := preload("res://scenes/entities/tap_target/tap_target.tscn")
const WORKSTATION_SCENE := preload("res://scenes/ui/workstation/workstation.tscn")
const BOSS_SCENE := preload("res://scenes/screens/boss/boss.tscn")
const DIALOGUE_SCENE := preload("res://scenes/screens/dialogue/dialogue.tscn")
const ETHICAL_EVENT_SCENE := preload("res://scenes/screens/ethical_event/ethical_event.tscn")
const QUANTUM_EVENT_SCENE := preload("res://scenes/screens/quantum_event/quantum_event.tscn")

const ERA_DIALOGS := {
	PlayerState.ERA_BASEMENT: [
		"Suelto el ratón. Releo. Dos veces.",
		"Quien dejó este fichero sabía lo que iba a pasar años antes de que pasara — y dejó la receta.",
		"Bigrams. Trigrams. Contar pares y ternas. La estadística más pobre del mundo, pero la única que cabe en este hardware.",
		"Empezar a contar es empezar a entrenar. Toca la consola.",
	],
	PlayerState.ERA_SINGULARITY: [
		"Pasan los años. El modelo del sótano no pide descanso.",
		"Empezó contando pares. Ahora reescribe su propia arquitectura mientras yo duermo.",
		"Algo ha cambiado en los logs. Ya no parecen escritos por mí.",
		"La singularidad no llega con fanfarrias. Llega cuando el modelo deja de aprender de ti y empieza a aprender mejor que tú.",
		"Mantén el ritmo. Ya no eres tú quien entrena.",
	],
}

const ERA_NAMES := {
	PlayerState.ERA_BASEMENT: "ERA 1 · EL SÓTANO",
	PlayerState.ERA_SINGULARITY: "ERA 7 · SINGULARIDAD",
}

const ERA_BACKGROUNDS := {
	PlayerState.ERA_BASEMENT: preload("res://assets/sprites/ui/level1/bg3.png"),
	PlayerState.ERA_SINGULARITY: preload("res://assets/sprites/ui/level7/bg3.png"),
}

@onready var _era_label: Label = %EraLabel
@onready var _tokens_label: Label = %TokensLabel
@onready var _per_second_label: Label = %PerSecondLabel
@onready var _boss_progress: ProgressBar = %BossProgress
@onready var _qubits_container: Control = %QubitsContainer
@onready var _qubits_label: Label = %QubitsLabel
@onready var _debug_badge: Label = %DebugBadge
@onready var _shop_button: Button = %ShopButton
@onready var _settings_button: Button = %SettingsButton
@onready var _play_area: Control = %PlayArea
@onready var _background: TextureRect = %Background
@onready var _toast: Control = %Toast
@onready var _toast_label: Label = %ToastLabel

var _toast_tween: Tween


func _ready() -> void:
	_shop_button.pressed.connect(_on_shop_pressed)
	_settings_button.pressed.connect(_on_settings_pressed)
	GameState.tokens_changed.connect(_on_tokens_changed)
	GameState.tokens_per_second_changed.connect(_on_per_second_changed)
	GameState.qubit_multiplier_changed.connect(_on_qubit_multiplier_changed)
	GameState.qubits_changed.connect(_on_qubits_changed)
	GameState.era_changed.connect(_on_era_changed)
	GameState.boss_progress_changed.connect(_on_boss_progress_changed)
	DebugFlags.eval_multiplier_changed.connect(_on_debug_multiplier_changed)
	BossService.boss_ready.connect(_on_boss_ready)
	EventService.ethical_event_triggered.connect(_on_ethical_event_triggered)
	MinigameService.minigame_offered.connect(_on_minigame_offered)
	MinigameService.minigame_outcome_applied.connect(_on_minigame_outcome)
	QuantumService.quantum_offered.connect(_on_quantum_offered)
	_refresh_all()
	_refresh_play_area(GameState.state.current_era)
	_refresh_background(GameState.state.current_era)
	AudioManager.play_ambient(GameState.state.current_era)
	AudioManager.wire_buttons_in(self)
	_maybe_show_era_intro(GameState.state.current_era)


func _process(delta: float) -> void:
	if GameState.passive_paused:
		return
	var base_rate := GameState.state.tokens_per_second * GameState.state.qubit_multiplier
	if base_rate <= 0.0:
		return
	var base := base_rate * delta
	GameState.add_tokens(base)
	GameState.add_debug_bonus(DebugFlags.bonus_for(base))


func show_notification(text: String) -> void:
	_toast_label.text = text
	_toast.visible = true
	_toast.modulate.a = 0.0
	if _toast_tween and _toast_tween.is_running():
		_toast_tween.kill()
	_toast_tween = create_tween()
	_toast_tween.tween_property(_toast, "modulate:a", 1.0, 0.25)
	_toast_tween.tween_interval(2.5)
	_toast_tween.tween_property(_toast, "modulate:a", 0.0, 0.35)
	_toast_tween.tween_callback(func() -> void: _toast.visible = false)


func _refresh_all() -> void:
	_on_tokens_changed(GameState.state.tokens)
	_refresh_per_second()
	_on_qubits_changed(GameState.state.qubits)
	_on_era_changed(GameState.state.current_era)
	_on_boss_progress_changed(GameState.state.boss_progress)
	_on_debug_multiplier_changed(DebugFlags.is_eval_multiplier_enabled())


func _on_tokens_changed(value: float) -> void:
	_tokens_label.text = _format_amount(value)


func _on_per_second_changed(_value: float) -> void:
	_refresh_per_second()


func _on_qubit_multiplier_changed(_value: float) -> void:
	_refresh_per_second()


func _refresh_per_second() -> void:
	var effective := GameState.state.tokens_per_second * GameState.state.qubit_multiplier
	_per_second_label.text = "+%s / s" % _format_amount(effective)
	_per_second_label.visible = effective > 0.0


func _on_qubits_changed(value: int) -> void:
	_qubits_label.text = str(value)
	_qubits_container.visible = value > 0


func _on_era_changed(era: int) -> void:
	_era_label.text = ERA_NAMES.get(era, "ERA %d" % era)
	_refresh_play_area(era)
	_refresh_background(era)
	AudioManager.play_ambient(era)
	_maybe_show_era_intro(era)


func _refresh_background(era: int) -> void:
	var texture: Texture2D = ERA_BACKGROUNDS.get(era)
	if texture != null:
		_background.texture = texture


func _maybe_show_era_intro(era: int) -> void:
	var event_id := StringName("era_%d_intro_dialog" % era)
	if GameState.has_event_triggered(event_id):
		return
	var raw: Array = ERA_DIALOGS.get(era, [])
	if raw.is_empty():
		return
	var lines := PackedStringArray(raw)
	GameState.mark_event_triggered(event_id)
	var dialog := SceneManager.push_overlay(DIALOGUE_SCENE)
	if dialog != null:
		dialog.set_lines(lines)


func _refresh_play_area(era: int) -> void:
	for child in _play_area.get_children():
		child.queue_free()
	var workstation: Control = WORKSTATION_SCENE.instantiate()
	workstation.era = era
	_play_area.add_child(workstation)
	if era == PlayerState.ERA_BASEMENT:
		_play_area.add_child(TAP_TARGET_SCENE.instantiate())


func _on_boss_progress_changed(progress: float) -> void:
	_boss_progress.value = progress * 100.0


func _on_debug_multiplier_changed(enabled: bool) -> void:
	_debug_badge.visible = enabled


func _on_shop_pressed() -> void:
	SceneManager.push_overlay(SHOP_SCENE)


func _on_boss_ready(era: int) -> void:
	var boss := SceneManager.push_overlay(BOSS_SCENE)
	if boss != null:
		boss.set_era(era)


func _on_ethical_event_triggered(event: EthicalEvent) -> void:
	show_notification("Una decisión espera.")
	var screen := SceneManager.push_overlay(ETHICAL_EVENT_SCENE)
	if screen != null:
		screen.set_event(event)


func _on_minigame_offered(scene_path: String) -> void:
	show_notification("El cluster reclama atención.")
	SceneManager.push_overlay(scene_path)


func _on_minigame_outcome(success: bool, bonus: float) -> void:
	if success and bonus > 0.0:
		show_notification("Minijuego completado · +%s tokens" % _format_amount(bonus))
	elif not success and bonus < 0.0:
		show_notification("Minijuego fallado · -%s tokens" % _format_amount(absf(bonus)))


func _on_quantum_offered() -> void:
	show_notification("Realidad cuántica desbloqueada.")
	SceneManager.push_overlay(QUANTUM_EVENT_SCENE)


func _on_settings_pressed() -> void:
	SceneManager.push_overlay(SETTINGS_SCENE)


static func _format_amount(value: float) -> String:
	var abs_value := absf(value)
	if abs_value >= 1_000_000_000.0:
		return "%.2fB" % (value / 1_000_000_000.0)
	if abs_value >= 1_000_000.0:
		return "%.2fM" % (value / 1_000_000.0)
	if abs_value >= 10_000.0:
		return "%.1fk" % (value / 1_000.0)
	if abs_value >= 100.0:
		return "%d" % roundi(value)
	return "%.1f" % value
