class_name UpgradeData
extends Resource

## Definición declarativa de una mejora.
##
## Cada mejora es un .tres en data/upgrades/. El UpgradeService los carga al
## arrancar y resuelve coste, disponibilidad y aplicación de efecto.

enum EffectType {
	NONE,
	TOKENS_PER_TAP_ADD,
	TOKENS_PER_TAP_MULT,
	TOKENS_PER_SECOND_ADD,
	TOKENS_PER_SECOND_MULT,
	QUBIT_MULTIPLIER_ADD,
}

## max_level = 1 → mejora binaria de un solo uso.
## max_level > 1 → mejora acumulable con tope.
## max_level = -1 → mejora acumulable sin tope.
const LEVEL_INFINITE := -1

@export var id: StringName = &""
@export var display_name: String = ""
@export_multiline var description: String = ""
@export var era: int = 1
@export var base_cost: float = 10.0
@export_range(1.0, 5.0, 0.01) var cost_growth: float = 1.15
@export var max_level: int = 1
@export var effect_type: EffectType = EffectType.NONE
@export var effect_value: float = 0.0
@export var prerequisite_id: StringName = &""
@export var icon: Texture2D


func cost_at_level(level: int) -> float:
	return base_cost * pow(cost_growth, max(0, level))


func is_max_level(level: int) -> bool:
	if max_level == LEVEL_INFINITE:
		return false
	return level >= max_level
