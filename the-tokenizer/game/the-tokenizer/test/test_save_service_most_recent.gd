extends GutTest

## Cubre SaveService.most_recent_slot, que es lo que "Continuar" usa para
## saltarse el selector. Verifica que escoge el slot con timestamp más
## alto, que devuelve 0 si no hay nada guardado y que el desempate va al
## slot activo.

const SAVE_VERSION := 1
const MAX_SLOTS := 3


func before_each() -> void:
	_clear_all_slots()


func after_each() -> void:
	_clear_all_slots()
	SaveService.set_active_slot(0)


func test_returns_zero_when_no_saves() -> void:
	assert_eq(SaveService.most_recent_slot(), 0,
		"sin partidas guardadas, most_recent_slot debe devolver 0")


func test_returns_only_occupied_slot() -> void:
	_write_save(2, 100)
	assert_eq(SaveService.most_recent_slot(), 2)


func test_picks_slot_with_highest_timestamp() -> void:
	_write_save(1, 100)
	_write_save(3, 500)
	_write_save(2, 250)
	assert_eq(SaveService.most_recent_slot(), 3)


func test_tie_breaks_in_favor_of_active_slot() -> void:
	_write_save(1, 300)
	_write_save(2, 300)
	SaveService.set_active_slot(2)
	assert_eq(SaveService.most_recent_slot(), 2,
		"con timestamps idénticos gana el slot activo")


func _write_save(slot: int, ts: int) -> void:
	var path := "user://savegame_%d.json" % slot
	var payload := {
		"version": SAVE_VERSION,
		"timestamp": ts,
		"state": {"current_era": 1, "tokens": 0.0, "qubits": 0},
	}
	var file := FileAccess.open(path, FileAccess.WRITE)
	assert_not_null(file, "no se pudo escribir el slot %d para el test" % slot)
	if file == null:
		return
	file.store_string(JSON.stringify(payload))
	file.close()


func _clear_all_slots() -> void:
	for slot in range(1, MAX_SLOTS + 1):
		var path := "user://savegame_%d.json" % slot
		if FileAccess.file_exists(path):
			DirAccess.remove_absolute(path)
