class_name GameScreen
extends Node2D

signal score_changed(new_score: int)
signal money_changed(new_money: int)

const GAME_OVER_DELAY: float = 1.8
const GAME_OVER_FADE: float = 0.7

enum GamePhase { PLAYING, PAUSED, GAME_OVER }

@onready var difficulty_manager: DifficultyManager = $DifficultyManager
@onready var missile_spawner: MissileSpawner = $MissileSpawner
@onready var powerup_manager: PowerupManager = $PowerupManager
@onready var defense_base: DefenseBase = $DefenseBase
@onready var hud: HUD = $HUD
@onready var shop: Shop = $Shop
@onready var level_up_banner: LevelUpBanner = $LevelUpBanner
@onready var in_game_settings: InGameSettings = $InGameSettings

var _cities: Array[City] = []
var _phase: GamePhase = GamePhase.PLAYING
var _overlay_count: int = 0

var _score: int = 0
var _money: int = 0
var _cities_alive: int = 0


func _ready() -> void:
	_cities = []
	for node in get_tree().get_nodes_in_group("cities"):
		if node is City:
			_cities.append(node as City)
	_cities_alive = _cities.size()
	difficulty_manager.reset()
	missile_spawner.set_cities(_cities)
	missile_spawner.missile_added.connect(_on_missile_added)
	for city: City in _cities:
		city.destroyed.connect(_on_city_destroyed)
		city.rebuilt.connect(_on_city_rebuilt)
	AudioManager.play_music("main-theme")
	CursorManager.set_game_cursor()
	FalloutStyle.add_scanline_overlay(self)
	if hud:
		hud.shop_requested.connect(_on_shop_requested)
		hud.emp_activated.connect(_on_emp_activated)
		hud.settings_requested.connect(_on_settings_requested)
		_connect_city_health_to_hud()
	if shop:
		shop.opened.connect(_on_overlay_opened)
		shop.closed.connect(_on_overlay_closed)
		shop.powerup_purchased.connect(_on_powerup_purchased)
	if in_game_settings:
		in_game_settings.opened.connect(_on_overlay_opened)
		in_game_settings.closed.connect(_on_overlay_closed)
	difficulty_manager.wave_started.connect(_on_wave_started)
	difficulty_manager.wave_started.connect(missile_spawner.mark_wave_pending)


func _process(_delta: float) -> void:
	if hud and defense_base:
		hud.update_cooldown(defense_base.get_cooldown_progress())


func _input(event: InputEvent) -> void:
	if _phase != GamePhase.PLAYING:
		return
	if event.is_action_pressed("shoot"):
		var pos: Vector2
		if event is InputEventMouseButton:
			pos = (event as InputEventMouseButton).position
		elif event is InputEventScreenTouch:
			pos = (event as InputEventScreenTouch).position
		else:
			return
		defense_base.shoot_at(pos)
	elif event.is_action_released("shoot"):
		defense_base.release()
	elif event.is_action_pressed("open_shop"):
		_on_shop_requested()
	elif event.is_action_pressed("fire_emp"):
		if hud and hud.is_emp_available():
			_on_emp_activated()


func get_cities() -> Array[City]:
	return _cities


func get_score() -> int:
	return _score


func get_money() -> int:
	return _money


func add_score(amount: int) -> void:
	_score += amount
	score_changed.emit(_score)


func add_money(amount: int) -> void:
	_money += amount
	money_changed.emit(_money)


func spend_money(amount: int) -> bool:
	if _money < amount:
		return false
	_money -= amount
	money_changed.emit(_money)
	return true


func _connect_city_health_to_hud() -> void:
	for city: City in _cities:
		city.health_changed.connect(
			func(h: int, m: int) -> void: hud.update_city_health(city.city_index, h, m)
		)


func _set_phase(new_phase: GamePhase) -> void:
	_phase = new_phase
	get_tree().paused = new_phase != GamePhase.PLAYING


func _on_overlay_opened() -> void:
	_overlay_count += 1
	if _overlay_count == 1:
		_set_phase(GamePhase.PAUSED)


func _on_overlay_closed() -> void:
	_overlay_count = max(0, _overlay_count - 1)
	if _overlay_count == 0 and _phase == GamePhase.PAUSED:
		_set_phase(GamePhase.PLAYING)


func _on_missile_added(missile: EnemyMissile) -> void:
	missile.destroyed.connect(_on_missile_destroyed)


func _on_missile_destroyed(score: int, money: int) -> void:
	add_score(score)
	add_money(money)


func _on_city_destroyed() -> void:
	_cities_alive -= 1
	if _cities_alive <= 0:
		_on_game_over()


func _on_city_rebuilt() -> void:
	_cities_alive += 1


func _on_game_over() -> void:
	_set_phase(GamePhase.GAME_OVER)
	AudioManager.stop_music()
	AudioManager.play_voice("game-over")
	await get_tree().create_timer(GAME_OVER_DELAY, true).timeout

	var fade_layer := CanvasLayer.new()
	fade_layer.layer = 100
	add_child(fade_layer)
	var fade_rect := ColorRect.new()
	fade_rect.color = Color(0.0, 0.0, 0.0, 0.0)
	fade_rect.mouse_filter = Control.MOUSE_FILTER_IGNORE
	fade_rect.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	fade_layer.add_child(fade_rect)

	var tween := create_tween().set_pause_mode(Tween.TWEEN_PAUSE_PROCESS)
	tween.tween_property(fade_rect, "color:a", 1.0, GAME_OVER_FADE)
	await tween.finished
	get_tree().paused = false
	GameOverScreen.pending_score = _score
	get_tree().change_scene_to_file(ScenePaths.GAME_OVER)


func _on_wave_started(_wave_number: int) -> void:
	if level_up_banner:
		level_up_banner.show_level_up()


func _on_shop_requested() -> void:
	if shop:
		shop.open(_cities)


func _on_settings_requested() -> void:
	if in_game_settings:
		in_game_settings.open()


func _on_powerup_purchased(powerup_id: int) -> void:
	if powerup_id == PowerupId.EMP:
		hud.set_emp_available(true)
	else:
		powerup_manager.apply_powerup(powerup_id, _cities)


func _on_emp_activated() -> void:
	powerup_manager.apply_powerup(PowerupId.EMP, _cities)
