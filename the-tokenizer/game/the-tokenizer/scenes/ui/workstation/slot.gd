extends Control

## Slot del monitor de la workstation asociado a una UpgradeData concreta.
##
## La workstation instancia uno por cada mejora con workstation_position no
## nula, lo posiciona sobre el marco y le pasa la UpgradeData con bind(). El
## slot consulta GameState.get_upgrade_level para decidir si mostrar el icono
## bloqueado o desbloqueado y si pinta el badge con el nivel actual.

@onready var _icon: TextureRect = %SlotIcon
@onready var _badge: Label = %SlotBadge

var _upgrade: UpgradeData


func bind(upgrade: UpgradeData) -> void:
	_upgrade = upgrade
	if not is_node_ready():
		await ready
	refresh()


func refresh() -> void:
	if _upgrade == null:
		return
	var level := GameState.get_upgrade_level(_upgrade.id)
	if level <= 0:
		_icon.texture = _upgrade.workstation_locked_icon
		_badge.visible = false
		return
	_icon.texture = _upgrade.workstation_icon
	_badge.text = _badge_text(level)
	_badge.visible = _upgrade.max_level != 1


func _badge_text(level: int) -> String:
	if _upgrade.max_level == UpgradeData.LEVEL_INFINITE:
		return "Lv %d" % level
	return "%d/%d" % [level, _upgrade.max_level]
