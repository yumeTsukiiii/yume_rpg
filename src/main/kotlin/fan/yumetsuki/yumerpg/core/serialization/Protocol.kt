package fan.yumetsuki.yumerpg.core.serialization

import kotlinx.serialization.json.*

sealed interface RpgSerialization<Content, Serializable> {
    fun encodeToContent(serializable: Serializable): Content

}

sealed interface RpgDeserialization<Content, Serializable> {
    fun decodeFromContent(content: Content) : RpgObject

}

/**
 * Rpg 对象协议，用来实现游戏中定义的对象的反序列化
 */
sealed interface RpgObjectProtocol<Content> : RpgDeserialization<Content, RpgElement>

/**
 * Rpg 元素协议，用来实现游戏中实际存在对象的序列化和反序列化的规则
 * @author yumetsuki
 */
sealed interface RpgElementProtocol<Content> : RpgSerialization<Content, RpgObject>, RpgDeserialization<Content, RpgObject>

class RpgJsonProtocol : RpgElementProtocol<String> {
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