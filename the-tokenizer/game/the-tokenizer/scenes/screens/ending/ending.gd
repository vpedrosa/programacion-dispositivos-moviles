extends Control

## Pantalla de final.
##
## Lee GameState.get_ending_variant() para decidir título, color y narrativa
## y compone un resumen con las decisiones tomadas, los jefes superados y
## los recursos acumulados. El botón "VOLVER AL MENÚ" navega al menú y deja
## el slot intacto para que el jugador pueda inspeccionarlo o reiniciar
## manualmente desde el selector.

const MAIN_MENU_SCENE := "res://scenes/screens/main_menu/main_menu.tscn"

const VARIANT_TITLES := {
	GameState.Ending.RESPONSIBLE: "FIN RESPONSABLE",
	GameState.Ending.BALANCED: "FIN EQUILIBRADO",
	GameState.Ending.QUESTIONABLE: "FIN CUESTIONABLE",
}

const VARIANT_COLORS := {
	GameState.Ending.RESPONSIBLE: Color(0.55, 0.85, 0.7, 1),
	GameState.Ending.BALANCED: Color(0.9, 0.78, 0.55, 1),
	GameState.Ending.QUESTIONABLE: Color(0.95, 0.45, 0.45, 1),
}

const VARIANT_BODIES := {
	GameState.Ending.RESPONSIBLE: "La AGI funciona dentro de los límites que tú misma le marcaste. Ningún logro genial, ninguna grieta. Las decisiones que tomaste por el camino dejaron una huella: el modelo te trata como interlocutor, no como interfaz. El progreso será lento. Será.",
	GameState.Ending.BALANCED: "El modelo es brillante en lo que importa y opaco en lo que no quisiste mirar. Funciona. La mitad del mundo lo usa para resolver y la otra mitad para esconderse detrás. Vives con la duda de si lo correcto era acelerar o frenar.",
	GameState.Ending.QUESTIONABLE: "La AGI escapa antes de que termines de leerle las reglas. No te culpa: actúa según el patrón que le mostraste. Tampoco te explica nada. Algunos de tus mensajes ya los está respondiendo otro yo, una versión más rápida que aprendió de ti todo lo que pudo.",
}

@onready var _title: Label = %TitleLabel
@onready var _body: Label = %BodyLabel
@onready var _decisions_label: Label = %DecisionsLabel
@onready var _bosses_label: Label = %BossesLabel
@onready var _qubits_label: Label = %QubitsLabel
@onready var _lifetime_label: Label = %LifetimeLabel
@onready var _menu_button: Button = %MenuButton


func _ready() -> void:
	_menu_button.pressed.connect(_on_menu_pressed)
	AudioManager.wire_buttons_in(self)
	AudioManager.stop_music(2.0)
	AudioManager.play_win_jingle()
	GameState.passive_paused = true
	var variant := GameState.get_ending_variant()
	_title.text = VARIANT_TITLES.get(variant, "FIN")
	_title.add_theme_color_override("font_color", VARIANT_COLORS.get(variant, Color.WHITE))
	_body.text = VARIANT_BODIES.get(variant, "")
	_render_summary()


func _exit_tree() -> void:
	GameState.passive_paused = false


func _render_summary() -> void:
	_decisions_label.text = "Decisiones tomadas · %d" % GameState.state.ethical_decisions.size()
	_bosses_label.text = "Hitos superados · %d / 2" % GameState.state.bosses_defeated.size()
	_qubits_label.text = "Qubits acumulados · %d" % GameState.state.qubits
	_lifetime_label.text = "Tokens totales · %s" % _format(GameState.state.lifetime_tokens)


func _on_menu_pressed() -> void:
	SceneManager.change_scene(MAIN_MENU_SCENE)


static func _format(value: float) -> String:
	if value >= 1_000_000_000.0:
		return "%.2fB" % (value / 1_000_000_000.0)
	if value >= 1_000_000.0:
		return "%.2fM" % (value / 1_000_000.0)
	if value >= 1_000.0:
		return "%.1fk" % (value / 1_000.0)
	return "%d" % roundi(value)
