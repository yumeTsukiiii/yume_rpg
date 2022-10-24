package fan.yumetsuki.yumerpg.utils

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive

fun JsonObjectBuilder.put(key: String, value: Any?) {
    when(value) {
        is String -> put(key, JsonPrimitive(value))
        is Number -> put(key, JsonPrimitive(value))
        is Boolean -> put(key, JsonPrimitive(value))
        is JsonElement -> put(key, value)
    }
}