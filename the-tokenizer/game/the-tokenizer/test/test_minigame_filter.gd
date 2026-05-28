extends GutTest

## Cubre que MinigameService filtra la refrigeración cuando aún no se
## ha comprado la Sala de servidores (#378). Backpropagation sigue
## disponible desde el principio.

const REFRIG_PATH := "res://scenes/minigames/refrigeration/refrigeration.tscn"
const BACKPROP_PATH := "res://scenes/minigames/backpropagation/backpropagation.tscn"


func before_each() -> void:
	GameState.reset()


func after_each() -> void:
	GameState.reset()


func test_refrigeration_excluded_without_serverroom() -> void:
	var available: Array = MinigameService._available_scenes()
	assert_does_not_have(available, REFRIG_PATH,
		"refrigeración no debería estar disponible sin sala de servidores")
	assert_has(available, BACKPROP_PATH,
		"backpropagation debería seguir disponible desde el inicio")


func test_refrigeration_included_after_buying_serverroom() -> void:
	GameState.increment_upgrade_level(&"era_1_serverroom")
	var available: Array = MinigameService._available_scenes()
	assert_has(available, REFRIG_PATH,
		"refrigeración debería estar disponible tras comprar la sala de servidores")
	assert_has(available, BACKPROP_PATH)
