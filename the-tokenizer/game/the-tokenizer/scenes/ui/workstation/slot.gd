extends Control

## Slot del monitor de la workstation asociado a una UpgradeData concreta.
##
## La workstation instancia uno por cada mejora con workstation_position no
## nula, lo posiciona sobre el marco y le pasa la UpgradeData con bind(). El
## slot consulta GameState.get_upgrade_level para decidir si mostrar el icono
## bloqueado o desbloqueado y si pinta el badge con el nivel actual. La primera
## vez que la mejora pasa de Lv0 a Lv1 se dispara una animación de desbloqueo
## (flash + scale-up) acompañada del SFX de compra; niveles posteriores se
## limitan a actualizar el badge.

const UNLOCK_PEAK_SCALE := Vector2(1.15, 1.15)
const UNLOCK_FLASH_COLOR := Color(2.0, 2.0, 2.0, 1.0)
const UNLOCK_RISE_DURATION := 0.12
const UNLOCK_FALL_DURATION := 0.18

@onready var _icon: TextureRect = %SlotIcon
@onready var _badge: Label = %SlotBadge

var _upgrade: UpgradeData
var _level: int = 0
var _unlock_tween: Tween


func bind(upgrade: UpgradeData) -> void:
	_upgrade = upgrade
	if not is_node_ready():
		await ready
	_level = GameState.get_upgrade_level(_upgrade.id)
	_render()


func refresh() -> void:
	if _upgrade == null:
		return
	var new_level := GameState.get_upgrade_level(_upgrade.id)
	var was_locked := _level <= 0
	_level = new_level
	_render()
	if was_locked and new_level >= 1:
		_play_unlock_animation()


func _render() -> void:
	if _level <= 0:
		_icon.texture = _upgrade.workstation_locked_icon
		_badge.visible = false
		return
	_icon.texture = _upgrade.workstation_icon
	_badge.text = _badge_text(_level)
	_badge.visible = _upgrade.max_level != 1


func _badge_text(level: int) -> String:
	if _upgrade.max_level == UpgradeData.LEVEL_INFINITE:
		return "Lv %d" % level
	return "%d/%d" % [level, _upgrade.max_level]


func _play_unlock_animation() -> void:
	if _unlock_tween != null and _unlock_tween.is_valid():
		_unlock_tween.kill()
	var pivot := size * 0.5 if size != Vector2.ZERO else custom_minimum_size * 0.5
	pivot_offset = pivot
	scale = Vector2.ONE
	modulate = Color.WHITE
	_unlock_tween = create_tween().set_parallel(true)
	_unlock_tween.tween_property(self, "scale", UNLOCK_PEAK_SCALE, UNLOCK_RISE_DURATION).set_ease(Tween.EASE_OUT)
	(_unlock_tween.tween_property(self, "scale", Vector2.ONE, UNLOCK_FALL_DURATION)
		.set_delay(UNLOCK_RISE_DURATION)
		.set_ease(Tween.EASE_IN))
	_unlock_tween.tween_property(self, "modulate", UNLOCK_FLASH_COLOR, UNLOCK_RISE_DURATION).set_ease(Tween.EASE_OUT)
	(_unlock_tween.tween_property(self, "modulate", Color.WHITE, UNLOCK_FALL_DURATION)
		.set_delay(UNLOCK_RISE_DURATION)
		.set_ease(Tween.EASE_IN))
	AudioManager.play_button_sfx()
