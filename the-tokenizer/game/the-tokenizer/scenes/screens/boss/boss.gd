extends Control

## Pantalla del hito técnico que cierra una era.
##
## Se instancia como overlay cuando BossService emite boss_ready. Al
## confirmar se marca el jefe como vencido, se cambia a la siguiente era
## prevista en NEXT_ERA y se cierra el overlay.

const ENDING_SCENE := "res://scenes/screens/ending/ending.tscn"
const INTRO_ERA7_SCENE := "res://scenes/screens/intro_era7/intro_era7.tscn"

const BOSS_TITLES := {
	1: "EL PRIMER MODELO",
	7: "AGI",
}

const BOSS_DESCRIPTIONS := {
	1: "Encadenas Word2Vec sobre un corpus de Wikipedia, dejas que el cluster murmure toda la noche y por la mañana la pantalla del CRT te devuelve la primera frase coherente que nunca pediste. Es 2013. Acabas de entrenar el primer modelo de lenguaje funcional de tu sótano.\n\nNo eres el único. Decides cerrar este capítulo y saltar adelante.",
	7: "Tu modelo deja de necesitarte. Empieza a optimizar su propio entrenamiento, a auto-corregirse y a escribir versiones mejores de sí mismo. La AGI ha llegado, y se llama como tú quieras llamarla.\n\nMira atrás. Las decisiones que tomaste por el camino son las que dictan lo que viene ahora.",
}

const PROMPT_BY_ERA := {
	1: "CONTINUAR",
	7: "VER EL FINAL",
}

@onready var _title: Label = %TitleLabel
@onready var _description: Label = %DescriptionLabel
@onready var _confirm_button: Button = %ConfirmButton

var _era: int = 1


func _ready() -> void:
	_confirm_button.pressed.connect(_on_confirm_pressed)
	AudioManager.wire_buttons_in(self)


func set_era(era: int) -> void:
	_era = era
	if not is_node_ready():
		await ready
	_title.text = BOSS_TITLES.get(era, "Hito Era %d" % era)
	_description.text = BOSS_DESCRIPTIONS.get(era, "")
	_confirm_button.text = PROMPT_BY_ERA.get(era, "CONTINUAR")


func _on_confirm_pressed() -> void:
	GameState.mark_boss_defeated(_era)
	SceneManager.pop_overlay()
	match _era:
		PlayerState.ERA_BASEMENT:
			SceneManager.change_scene(INTRO_ERA7_SCENE)
		PlayerState.ERA_SINGULARITY:
			SceneManager.change_scene(ENDING_SCENE)
