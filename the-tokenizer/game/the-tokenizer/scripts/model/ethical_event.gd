class_name EthicalEvent
extends Resource

## Dilema ético serializable.
##
## Cada elemento de [member choices] es un Dictionary con las claves:
##  * id (String) — identificador de la opción
##  * label (String) — texto del botón
##  * weight (int) — peso ético (positivo responsable, negativo cuestionable)
##  * feedback (String) — narrativa breve mostrada tras elegir

@export var id: StringName = &""
@export_multiline var prompt: String = ""
@export var trigger_era: int = 1
@export var trigger_threshold: float = 0.0
@export var choices: Array = []
