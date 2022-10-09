package fan.yumetsuki.yumerpg.core.utils

import fan.yumetsuki.yumerpg.core.script.ScriptSerializable
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.put

/**
 * 仅向 JsonObject 中 put 原始类型数据
 */
fun JsonObjectBuilder.putSerializable(name: String, value: Any?) {

    when(value) {
        is Number -> put(name, value)
        is String -> put(name, value)
        is Boolean -> put(name, value)
        is ScriptSerializable -> put(name, value.toScriptObj())
    }

}