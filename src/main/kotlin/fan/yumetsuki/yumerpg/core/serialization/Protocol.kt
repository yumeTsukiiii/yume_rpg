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
sealed interface RpgElementProtocol<Content, ElementParam> : RpgDeserializationProtocol<Content, RpgElement<ElementParam>>

/**
 * Rpg 元素协议，用来实现游戏中实际存在对象的序列化和反序列化的规则
 * @author yumetsuki
 */
sealed interface RpgObjectProtocol<Content> : RpgSerializationProtocol<Content, RpgObject>, RpgDeserializationProtocol<Content, RpgObject>

/**
 * RpgElement 反序列化协议的 JSON 实现
 * {
 *  "id": Long, // RpgElement id，必填
 *  "builder": Long, // 构建对应的 RpgObject 的 builderId，必填
 *  "param": JsonObject? // 参数，由 builder 生成 RpgObject 时动态读取解析
 * }
 * @author
 */
object RpgElementJsonProtocol : RpgElementProtocol<String, JsonObject> {

    override fun decodeFromContent(content: String): RpgElement<JsonObject> {
        return when (val json = Json.parseToJsonElement(content)) {
            is JsonArray -> RpgElementArray(json.filterIsInstance<JsonObject>().map(this::decodeToRpgElement))
            is JsonObject -> decodeToRpgElement(json)
            is JsonPrimitive -> error("仅支持 JsonArray 和 JsonObject 协议")
        }
    }

    private fun decodeToRpgElement(json: JsonObject): RpgElement<JsonObject> {
        val id = (json["id"] as? JsonPrimitive)?.content?.toLong()!!
        val builderId = (json["constructor"] as? JsonPrimitive)?.content?.toLong()!!
        val param = (json["param"] as? JsonObject)
        return JsonRpgElement(id, builderId, param)
    }

    private class JsonRpgElement(
        override val id: Long,
        override val constructorId: Long,
        /**
         * RpgElement 协议中定义的 param
         */
        private val param: JsonObject?
    ) : RpgElement<JsonObject> {

        override fun createRpgObject(rpgElementContext: RpgElementContext<JsonObject>): RpgObject {
            return rpgElementContext.getConstructor(constructorId).construct(
                param?.let {
                    // 延迟创建 RpgObject 时，需要确保 rpgElementCenter 中已经有系统所需的所有 RpgElement
                    JsonRpgObjContext(param, rpgElementContext)
                } ?: RpgObjectContext.Empty
            )
        }

    }

    private class JsonRpgObjContext(
        private val defaultParam: JsonObject?,
        private val rpgElementContext: RpgElementContext<JsonObject>,
    ) : RpgObjectContext {
        override val elementId: Long
            get() = rpgElementContext.current.id

        override fun getIntOrNull(key: String): Int? {
            return getIntOrNull(key, rpgElementContext.data) ?: getIntOrNull(key, defaultParam)
        }

        override fun getStringOrNull(key: String): String? {
            return getStringOrNull(key, rpgElementContext.data) ?: getStringOrNull(key, defaultParam)
        }

        override fun getDoubleOrNull(key: String): Double? {
            return getDoubleOrNull(key, rpgElementContext.data) ?: getDoubleOrNull(key, defaultParam)
        }

        override fun getBooleanOrNull(key: String): Boolean? {
            return getBooleanOrNull(key, rpgElementContext.data) ?: getBooleanOrNull(key, defaultParam)
        }

        override fun getRpgObjectOrNull(key: String): RpgObject? {
            return getRpgObjectOrNull(key, rpgElementContext.data) ?: getRpgObjectOrNull(key, defaultParam)
        }

        private fun getIntOrNull(key: String, param: JsonObject?): Int? {
            return (param?.get(key) as? JsonPrimitive)?.intOrNull ?: (defaultParam?.get(key) as? JsonPrimitive)?.intOrNull
        }

        private fun getStringOrNull(key: String, param: JsonObject?): String? {
            return (param?.get(key) as? JsonPrimitive)?.contentOrNull
        }

        private fun getDoubleOrNull(key: String, param: JsonObject?): Double? {
            return (param?.get(key) as? JsonPrimitive)?.doubleOrNull
        }

        private fun getBooleanOrNull(key: String, param: JsonObject?): Boolean? {
            return (param?.get(key) as? JsonPrimitive)?.booleanOrNull
        }

        private fun getRpgObjectOrNull(key: String, param: JsonObject?): RpgObject? {
            return param?.get(key)?.let {
                return when (it) {
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
            return rpgElementContext.getRpgElementOrNull(elementId)?.createRpgObject(rpgElementContext)
        }

        private fun decodeToRpgObject(json: JsonPrimitive): RpgObject? {
            return json.longOrNull?.let(this::decodeToRpgObject)
        }

        private fun decodeToRpgObject(json: JsonObject): RpgObject? {
            return (json["id"] as? JsonPrimitive)?.let(this::decodeToRpgObject)
        }
    }

}

/**
 * RpgObject 序列化、反序列化协议的 JSON 实现
 * {
 *  "elementId": Long, // RpgObject 对应创建者 RpgElement id，必填
 *  "data": JsonObject? // 存储参数，由 builder 生成 RpgObject 时动态读取解析，并在[encodeToContent]时存储，当无存档生成对象时，将由[RpgElement]中的参数填充
 * }
 * [RpgObject] 会由其对应的 [RpgElement] 创建，因此该协议必须依赖构建好的 Element 列表以及注册的 [RpgObjectConstructor] 列表
 * @author
 */
class RpgObjectJsonProtocol(
    private val rpgElementCenter: RpgElementCenter<JsonObject>,
    private val rpgObjConstructorCenter: RpgObjConstructorCenter<JsonObject>
): RpgObjectProtocol<String> {

    override fun encodeToContent(serializable: RpgObject): String {
        return buildJsonObject {
            rpgObjConstructorCenter.getConstructor(
                rpgElementCenter.getElement(serializable.elementId).constructorId
            ).deconstruct(serializable).also {
                put("elementId", it.elementId)
                it.data?.apply { put("data", this) }
            }
        }.toString()
    }

    override fun decodeFromContent(content: String): RpgObject {
        return when(val json = Json.parseToJsonElement(content)) {
            is JsonArray -> RpgObjectArray(json.filterIsInstance<JsonObject>().map(this::decodeFromJsonObject))
            is JsonObject -> decodeFromJsonObject(json)
            is JsonPrimitive -> error("协议内容必须是 JsonArray 或 JsonObject")
        }
    }

    private fun decodeFromJsonObject(json: JsonObject): RpgObject {
        val elementId = (json["elementId"] as? JsonPrimitive)?.content?.toLong()!!
        val data = (json["data"] as? JsonObject)
        return rpgElementCenter.getElement(elementId).run {
            createRpgObject(
                JsonRpgElementContext(this, data)
            )
        }
    }

    private inner class JsonRpgElementContext(
        override val current: RpgElement<JsonObject>,
        override val data: JsonObject?
    ) : RpgElementContext<JsonObject> {
        override fun getRpgElementOrNull(id: Long): RpgElement<JsonObject>? = rpgElementCenter.getElementOrNull(id)

        override fun getRpgObjectConstructorOrNull(id: Long): RpgObjectConstructor<JsonObject>? = rpgObjConstructorCenter.getConstructorOrNull(id)

    }

}

/**
 * 适配 Json 协议的 [RpgObjectData]，它适用于实现 Json 协议的 [RpgObjectConstructor]
 */
class JsonRpgObjectData(
    override val elementId: Long
) : RpgObjectData<JsonObject> {

    private val jsonData = mutableMapOf<String, JsonElement>()
    override val data: JsonObject?
        get() = if (jsonData.isEmpty()) {
            null
        } else {
            JsonObject(jsonData)
        }

    fun put(key: String, data: JsonElement) {
        jsonData[key] = data
    }

    fun put(key: String, data: RpgObjectData<JsonObject>) {
        jsonData[key] = buildJsonObject {
            put("elementId", data.elementId)
            data.data?.let { put("data", it) }
        }
    }

}