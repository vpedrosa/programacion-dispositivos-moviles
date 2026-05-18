extends Control

## Estación de trabajo del investigador.
##
## Se renderiza dentro del PlayArea del HUD y representa el monitor de la era:
## CRT verde fósforo en Era 1, holopanel cian/magenta en Era 7. Sobre el marco
## se instancia un slot por cada UpgradeData de la era con workstation_position
## definida; cada slot consulta GameState para pintarse bloqueado o desbloqueado
## y se refresca al recibir GameState.upgrade_levelup. La escena no captura
## inputs (mouse_filter = IGNORE) para no bloquear el tap manual de Era 1.

const FRAME_TEXTURES := {
	1: preload("res://assets/sprites/ui/workstation/frame_era1.png"),
	7: preload("res://assets/sprites/ui/workstation/frame_era7.png"),
}
const FRAME_DESIGN_SIZE := Vector2(800, 500)
const SLOT_SCENE := preload("res://scenes/ui/workstation/slot.tscn")

@export var era: int = 1

@onready var _frame_texture: TextureRect = %FrameTexture
@onready var _slots_layer: Control = %SlotsLayer

var _slots: Dictionary = {}


func _ready() -> void:
	_apply_era_texture()
	_spawn_slots()
	GameState.upgrade_levelup.connect(_on_upgrade_levelup)


func _apply_era_texture() -> void:
	var texture: Texture2D = FRAME_TEXTURES.get(era)
	if texture != null:
		_frame_texture.texture = texture


func _spawn_slots() -> void:
	for upgrade in UpgradeService.get_for_era(era):
		if upgrade.workstation_position == Vector2.ZERO:
			continue
		var slot: Control = SLOT_SCENE.instantiate()
		_slots_layer.add_child(slot)
		_position_slot(slot, upgrade.workstation_position)
		slot.bind(upgrade)
		_slots[upgrade.id] = slot


func _position_slot(slot: Control, design_position: Vector2) -> void:
	var anchor := design_position / FRAME_DESIGN_SIZE
	slot.anchor_left = anchor.x
	slot.anchor_top = anchor.y
	slot.anchor_right = anchor.x
	slot.anchor_bottom = anchor.y
	var half := slot.custom_minimum_size * 0.5
	slot.offset_left = -half.x
	slot.offset_top = -half.y
	slot.offset_right = half.x
	slot.offset_bottom = half.y


func _on_upgrade_levelup(upgrade_id: StringName, _new_level: int) -> void:
	var slot: Variant = _slots.get(upgrade_id)
	if slot != null:
		slot.refresh()
