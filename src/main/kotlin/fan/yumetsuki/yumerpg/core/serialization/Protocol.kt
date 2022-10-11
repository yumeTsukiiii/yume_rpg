package fan.yumetsuki.yumerpg.core.serialization

import kotlinx.serialization.json.*

/**
 * Rpg 游戏协议，用来实现序列化和反序列化的规则
 * @author yumetsuki
 */
sealed interface RpgProtocol<Content> {

    fun encodeToContent(rpgObject: RpgObject): Content

    fun decodeFromContent(content: Content) : RpgObject

}

class RpgJsonProtocol : RpgProtocol<String> {
    override fun encodeToContent(rpgObject: RpgObject): String {
        when(rpgObject) {
            is RpgObjectArray -> {

            }
            is RpgModel -> {

            }
            is RpgAbility<*, *, *, *> -> {

            }
        }
        TODO("Not yet implemented")
    }

    override fun decodeFromContent(content: String): RpgObject {
        return when(val json = Json.parseToJsonElement(content)) {
            is JsonArray -> RpgObjectArray(json.filterIsInstance<JsonObject>().map(this::decodeFromJsonObject))
            is JsonObject -> decodeFromJsonObject(json)
            is JsonPrimitive -> error("协议内容必须是 JsonArray 或 JsonObject")
        }
    }

    private fun decodeFromJsonObject(jsonObject: JsonObject): RpgObject {
        TODO("Not yet implemented")
    }

}