class_name ConfirmModal
extends Control

## Modal reutilizable de confirmación.
##
## Sustituye al [ConfirmationDialog] nativo de Godot, que vive en una ventana
## propia y no hereda el theme de la escena que lo invoca. El modal cubre toda
## la pantalla con un veil, presenta un panel centrado con título, mensaje y
## dos botones (confirmar / cancelar) usando los mismos sprites y fuentes que
## el resto del juego.
##
## API:
##   set_content(title, message, confirm_text, cancel_text)
##   show_modal()
##   hide_modal()
## Señales: [signal confirmed], [signal cancelled].

signal confirmed()
signal cancelled()

@onready var _title_label: Label = %TitleLabel
@onready var _message_label: Label = %MessageLabel
@onready var _confirm_button: Button = %ConfirmButton
@onready var _cancel_button: Button = %CancelButton


func _ready() -> void:
	hide()
	_confirm_button.pressed.connect(_on_confirm)
	_cancel_button.pressed.connect(_on_cancel)
	AudioManager.wire_buttons_in(self)


func set_content(title: String, message: String, confirm_text: String = "Confirmar", cancel_text: String = "Cancelar") -> void:
	# El nodo puede llamarse antes de _ready (ej. desde set_event-like setters).
	if not is_node_ready():
		await ready
	_title_label.text = title
	_message_label.text = message
	_confirm_button.text = confirm_text
	_cancel_button.text = cancel_text


func show_modal() -> void:
	show()


func hide_modal() -> void:
	hide()


func _on_confirm() -> void:
	hide_modal()
	confirmed.emit()


func _on_cancel() -> void:
	hide_modal()
	cancelled.emit()
