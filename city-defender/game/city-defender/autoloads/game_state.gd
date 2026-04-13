extends Node

signal score_changed(new_score: int)
signal money_changed(new_money: int)
signal game_over

var score: int = 0
var money: int = 0
var cities_alive: int = 4


func add_score(amount: int) -> void:
	score += amount
	score_changed.emit(score)


func add_money(amount: int) -> void:
	money += amount
	money_changed.emit(money)


func spend_money(amount: int) -> bool:
	if money >= amount:
		money -= amount
		money_changed.emit(money)
		return true
	return false


func notify_city_destroyed() -> void:
	cities_alive -= 1
	if cities_alive <= 0:
		game_over.emit()


func reset(city_count: int = 4) -> void:
	score = 0
	money = 0
	cities_alive = city_count
