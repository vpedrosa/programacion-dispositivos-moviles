class_name EnemyMissileFast
extends EnemyMissile


func _ready() -> void:
	super._ready()
	speed = 300.0
	score_value = 200
	money_value = 30
	max_hits = 1
