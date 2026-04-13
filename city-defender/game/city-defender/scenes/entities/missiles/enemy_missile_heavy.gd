class_name EnemyMissileHeavy
extends EnemyMissile


func _ready() -> void:
	super._ready()
	speed = 100.0
	score_value = 400
	money_value = 60
	max_hits = 2


func _on_hit_survived() -> void:
	modulate = Color(1.0, 0.4, 0.4)
