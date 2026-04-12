extends CanvasLayer

signal closed
signal powerup_purchased(powerup_id: String)

const POWERUPS: Dictionary = {
	"repair_city":   {"name": "Reparar ciudad",     "cost": 200, "desc": "Restaura vida a una ciudad dañada"},
	"rebuild_city":  {"name": "Reconstruir ciudad", "cost": 500, "desc": "Revive una ciudad destruida con HP mínimo"},
	"shield":        {"name": "Escudo temporal",    "cost": 300, "desc": "Escudo en todas las ciudades (absorbe 1 impacto)"},
	"radius_plus":   {"name": "Radio de explosión+","cost": 250, "desc": "Aumenta permanentemente el radio de explosión"},
	"double_shot":   {"name": "Disparo doble",      "cost": 350, "desc": "2 misiles por toque durante 15s"},
	"emp":           {"name": "Bomba EMP",          "cost": 600, "desc": "Destruye todos los misiles en pantalla"},
	"cooldown_plus": {"name": "Cadencia+",          "cost": 250, "desc": "Reduce permanentemente el cooldown de disparo"},
}

var _cities: Array = []


func open(cities: Array) -> void:
	_cities = cities
	visible = true
	get_tree().paused = true
	_refresh_buttons()


func close() -> void:
	visible = false
	get_tree().paused = false
	closed.emit()


func _refresh_buttons() -> void:
	# TODO: update button states based on GameState.money and city states
	pass


func _on_buy_pressed(powerup_id: String) -> void:
	var cost: int = POWERUPS[powerup_id]["cost"]
	if GameState.spend_money(cost):
		powerup_purchased.emit(powerup_id)
		_refresh_buttons()


func _on_close_button_pressed() -> void:
	close()
