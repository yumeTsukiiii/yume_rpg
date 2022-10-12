package fan.yumetsuki.yumerpg.core.serialization

import kotlinx.serialization.json.*

/**
 * Rpg 序列化协议
 * @author yumetsuki
 */
sealed interface RpgSerializationProtocol<Content, Serializable> {

    fun encodeToContent(serializable: Serializable): Content

}

/**
 * Rpg 反序列化协议
 * @author yumetsuki
 */
sealed interface RpgDeserializationProtocol<Content, Serializable> {

    fun decodeFromContent(content: Content) : Serializable

}

/**
 * Rpg 对象协议，用来实现游戏中定义的对象的反序列化
 */
sealed interface RpgElementProtocolProtocol<Content> : RpgDeserializationProtocol<Content, RpgElement>

/**
 * Rpg 元素协议，用来实现游戏中实际存在对象的序列化和反序列化的规则
 * @author yumetsuki
 */
sealed interface RpgObjectProtocolProtocolProtocol<Content> : RpgSerializationProtocol<Content, RpgObject>, RpgDeserializationProtocol<Content, RpgObject>

/**
 * RpgElement 反序列化协议的 JSON 实现
 * {
 *  "id": Long, // RpgElement id，必填
 *  "builder": Long, // 构建对应的 RpgObject 的 builderId，必填
 *  "param": JsonObject? // 参数，由 builder 生成 RpgObject 时动态读取解析
 * }
 * @author
 */
class RpgElementJsonProtocolProtocol(
    private val rpgObjBuilderCenter: RpgObjBuilderCenter,
    private val rpgElementCenter: MutableRpgElementCenter
): RpgElementProtocolProtocol<String> {

    override fun decodeFromContent(content: String): RpgElement {
        return when(val json = Json.parseToJsonElement(content)) {
            is JsonArray -> RpgElementArray(json.filterIsInstance<JsonObject>().map(this::decodeToRpgElement))
            is JsonObject -> decodeToRpgElement(json)
            is JsonPrimitive -> error("仅支持 JsonArray 和 JsonObject 协议")
        }
    }

    private fun decodeToRpgElement(json: JsonObject) : RpgElement {
        val id = (json["id"] as? JsonPrimitive)?.content?.toLong()!!
        val builderId = (json["builder"] as? JsonPrimitive)?.content?.toLong()!!
        val param = (json["param"] as? JsonObject)
        return JsonRpgElement(
            id,
            rpgObjBuilderCenter.getRpgObjectBuilder(builderId),
            param,
            rpgElementCenter
        )
    }

    private class JsonRpgElement(
        override val id: Long,
        private val builder: RpgObjectBuilder,
        private val param: JsonObject?,
        private val rpgElementCenter: MutableRpgElementCenter
    ) : RpgElement {

        init {
            // 创建 element 时注册自身
            rpgElementCenter.registerElement(id, this)
        }

        override fun createRpgObject(): RpgObject {
            return builder.build(
                param?.let {
                    // 延迟创建 RpgObject 时，需要确保 rpgElementCenter 中已经有系统所需的所有 RpgElement
                    JsonRpgObjBuildContext(param, rpgElementCenter)
                } ?: RpgObjectBuildContext.Empty
            )
        }

    }

    private class JsonRpgObjBuildContext(
        private val param: JsonObject,
        private val rpgElementCenter: RpgElementCenter
    ): RpgObjectBuildContext {

        override fun getIntOrNull(key: String): Int? {
            return (param[key] as? JsonPrimitive)?.intOrNull
        }

        override fun getStringOrNull(key: String): String? {
            return (param[key] as? JsonPrimitive)?.contentOrNull
        }

        override fun getDoubleOrNull(key: String): Double? {
            return (param[key] as? JsonPrimitive)?.doubleOrNull
        }

        override fun getBooleanOrNull(key: String): Boolean? {
            return (param[key] as? JsonPrimitive)?.booleanOrNull
        }

        override fun getRpgObjectOrNull(key: String): RpgObject? {
            return param[key]?.let {
                return when(it) {
                    is JsonArray -> RpgObjectArray(
                        it.mapNotNull { item ->
                            when (item) {
                                is JsonObject -> decodeToRpgObject(item)
                                is JsonPrimitive -> decodeToRpgObject(item)
                                else -> null
                            }
                        }
                    )
                    is JsonObject -> decodeToRpgObject(it)
                    is JsonPrimitive -> decodeToRpgObject(it)
                }
            }
        }

        private fun decodeToRpgObject(elementId: Long): RpgObject? {
            return rpgElementCenter.getElementOrNull(elementId)?.createRpgObject()
        }

        private fun decodeToRpgObject(json: JsonPrimitive): RpgObject? {
            return json.longOrNull?.let(this::decodeToRpgObject)
        }

        private fun decodeToRpgObject(json: JsonObject): RpgObject? {
            return (json["id"] as? JsonPrimitive)?.let(this::decodeToRpgObject)
        }
    }

}

class RpgObjectJsonProtocolProtocolProtocol : RpgObjectProtocolProtocolProtocol<String> {

    override fun encodeToContent(serializable: RpgObject): String {
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