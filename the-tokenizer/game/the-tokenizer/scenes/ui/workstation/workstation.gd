extends Control

## Estación de trabajo del investigador.
##
## Se renderiza dentro del PlayArea del HUD y representa el monitor de la era:
## CRT verde fósforo en Era 1, holopanel cian/magenta en Era 7. Sobre el marco
## se colocan 6 slots vacíos que en próximas issues se cablearán a las mejoras
## de la tienda. Esta escena no captura inputs (mouse_filter = IGNORE) para no
## bloquear la mecánica de tap manual que vive sobre ella en Era 1.

const FRAME_TEXTURES := {
	1: preload("res://assets/sprites/ui/workstation/frame_era1.png"),
	7: preload("res://assets/sprites/ui/workstation/frame_era7.png"),
}

@export var era: int = 1

@onready var _frame_texture: TextureRect = %FrameTexture


func _ready() -> void:
	_apply_era_texture()


func _apply_era_texture() -> void:
	var texture: Texture2D = FRAME_TEXTURES.get(era)
	if texture != null:
		_frame_texture.texture = texture
