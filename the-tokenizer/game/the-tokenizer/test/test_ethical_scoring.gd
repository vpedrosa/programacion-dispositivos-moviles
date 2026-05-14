extends GutTest

## Verifica los tres umbrales del scoring ético definidos en #317.
##
## RESPONSIBLE se devuelve cuando ethical_score >= RESPONSIBLE_THRESHOLD.
## QUESTIONABLE cuando ethical_score <= QUESTIONABLE_THRESHOLD.
## BALANCED en cualquier valor intermedio.

func before_each() -> void:
	GameState.state = PlayerState.new_default()


func test_balanced_when_no_decisions_recorded() -> void:
	assert_eq(GameState.state.ethical_score, 0)
	assert_eq(GameState.get_ending_variant(), GameState.Ending.BALANCED)


func test_responsible_above_positive_threshold() -> void:
	GameState.state.ethical_score = GameState.RESPONSIBLE_THRESHOLD
	assert_eq(GameState.get_ending_variant(), GameState.Ending.RESPONSIBLE)
	GameState.state.ethical_score = GameState.RESPONSIBLE_THRESHOLD + 5
	assert_eq(GameState.get_ending_variant(), GameState.Ending.RESPONSIBLE)


func test_balanced_just_below_responsible_threshold() -> void:
	GameState.state.ethical_score = GameState.RESPONSIBLE_THRESHOLD - 1
	assert_eq(GameState.get_ending_variant(), GameState.Ending.BALANCED)


func test_questionable_below_negative_threshold() -> void:
	GameState.state.ethical_score = GameState.QUESTIONABLE_THRESHOLD
	assert_eq(GameState.get_ending_variant(), GameState.Ending.QUESTIONABLE)
	GameState.state.ethical_score = GameState.QUESTIONABLE_THRESHOLD - 5
	assert_eq(GameState.get_ending_variant(), GameState.Ending.QUESTIONABLE)


func test_balanced_just_above_questionable_threshold() -> void:
	GameState.state.ethical_score = GameState.QUESTIONABLE_THRESHOLD + 1
	assert_eq(GameState.get_ending_variant(), GameState.Ending.BALANCED)


func test_record_decision_accumulates_weight() -> void:
	GameState.record_ethical_decision(&"era_1_dataset", &"filter_hard", 1)
	GameState.record_ethical_decision(&"era_7_evals", &"delay_publish", 1)
	GameState.record_ethical_decision(&"era_7_alignment", &"report_publicly", 1)
	assert_eq(GameState.state.ethical_score, 3)
	assert_eq(GameState.get_ending_variant(), GameState.Ending.RESPONSIBLE)


func test_record_decision_negative_weights_drop_to_questionable() -> void:
	GameState.record_ethical_decision(&"era_1_dataset", &"leave_raw", -1)
	GameState.record_ethical_decision(&"era_1_attribution", &"strip_credits", -1)
	GameState.record_ethical_decision(&"era_7_compute", &"buy_carbon", -1)
	assert_eq(GameState.state.ethical_score, -3)
	assert_eq(GameState.get_ending_variant(), GameState.Ending.QUESTIONABLE)


func test_neutral_weights_keep_balanced() -> void:
	for i in 5:
		GameState.record_ethical_decision(StringName("neutral_%d" % i), &"meh", 0)
	assert_eq(GameState.state.ethical_score, 0)
	assert_eq(GameState.get_ending_variant(), GameState.Ending.BALANCED)
