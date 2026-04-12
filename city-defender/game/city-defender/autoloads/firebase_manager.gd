extends Node

## Firebase Firestore manager for global highscores.
## Uses the Firestore REST API over HTTPS.
## Requires the project's Firebase config to be set below.

const _PROJECT_ID: String = ""  # TODO: set Firebase project ID
const _API_KEY: String = ""     # TODO: set Firebase Web API key
const _COLLECTION: String = "highscores"
const _TOP_COUNT: int = 10

var _http: HTTPRequest


func _ready() -> void:
	_http = HTTPRequest.new()
	add_child(_http)


## Returns an Array of Dictionaries [{name, score, timestamp}, ...] sorted by score desc.
func get_top_scores() -> Array:
	var url = _build_query_url()
	_http.request(url)
	var result = await _http.request_completed
	return _parse_scores(result[3])


## Returns true if score would enter the current top 10.
func is_top_10(score: int) -> bool:
	var scores = await get_top_scores()
	if scores.size() < _TOP_COUNT:
		return true
	return score > scores[-1].score


## Inserts a new entry into Firestore.
func submit_score(player_name: String, score: int) -> void:
	var url = _build_document_url()
	var body = JSON.stringify({
		"fields": {
			"name":      {"stringValue": player_name},
			"score":     {"integerValue": str(score)},
			"timestamp": {"integerValue": str(int(Time.get_unix_time_from_system()))}
		}
	})
	var headers = ["Content-Type: application/json"]
	_http.request(url, headers, HTTPClient.METHOD_POST, body)
	await _http.request_completed


func _build_query_url() -> String:
	return (
		"https://firestore.googleapis.com/v1/projects/%s/databases/(default)/documents/%s"
		% [_PROJECT_ID, _COLLECTION]
		+ "?orderBy=score%20desc&pageSize=%d&key=%s" % [_TOP_COUNT, _API_KEY]
	)


func _build_document_url() -> String:
	return (
		"https://firestore.googleapis.com/v1/projects/%s/databases/(default)/documents/%s?key=%s"
		% [_PROJECT_ID, _COLLECTION, _API_KEY]
	)


func _parse_scores(body: PackedByteArray) -> Array:
	var text = body.get_string_from_utf8()
	var json = JSON.parse_string(text)
	if json == null or not json.has("documents"):
		return []
	var result: Array = []
	for doc in json["documents"]:
		var fields = doc.get("fields", {})
		result.append({
			"name":  fields.get("name",  {}).get("stringValue",  ""),
			"score": int(fields.get("score", {}).get("integerValue", 0)),
		})
	return result
