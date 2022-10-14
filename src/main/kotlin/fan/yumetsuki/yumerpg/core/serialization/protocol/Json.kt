package fan.yumetsuki.yumerpg.core.serialization.protocol

import fan.yumetsuki.yumerpg.core.serialization.*
import kotlinx.serialization.json.*

/**
 * RpgElement 反序列化协议的 JSON 实现
 * {
 *  "id": Long, // RpgElement id，必填
 *  "builder": Long, // 构建对应的 RpgObject 的 builderId，必填
 *  "data": JsonObject? // 参数，由 builder 生成 RpgObject 时动态读取解析
 * }
 * @author
 */
object JsonRpgElementProtocol : RpgElementProtocol<String, JsonObject> {

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
        val param = (json["data"] as? JsonObject)
        return JsonRpgElement(id, builderId, param)
    }

}

/**
 * RpgObject 序列化、反序列化协议的 JSON 实现
 * {
 *  "elementId": Long, // RpgObject 对应创建者 RpgElement id，必填
 *  "data": JsonObject? // 存储参数，由 builder 生成 RpgObject 时动态读取解析，并在[encodeToContent]时存储，当无存档生成对象时，将由[RpgElement]中的参数填充
 * }
 * [RpgObject] 会由其对应的 [RpgElement] 创建，因此该协议必须依赖构建好的 Element 列表以及注册的 [RpgObjectConstructor] 列表
 * @author yumetsuki
 */
object JsonRpgObjectProtocol: RpgObjectProtocol<String, JsonObject> {

    override fun encodeToContent(
        rpgObjSerializeContext: RpgObjSerializeContext<JsonObject>,
        serializable: RpgObject
    ): String {
        return buildJsonObject {
            rpgObjSerializeContext.getRpgObjConstructor(
                rpgObjSerializeContext.getRpgElement(serializable.elementId).constructorId
            ).deconstruct(JsonRpgObjDeconstructContext(serializable.elementId, rpgObjSerializeContext, serializable)).also {
                put("elementId", it.elementId)
                it.data?.apply { put("data", this) }
            }
        }.toString()
    }

    override fun decodeFromContent(
        rpgObjSerializeContext: RpgObjSerializeContext<JsonObject>,
        content: String
    ): RpgObject {
        return when(val json = Json.parseToJsonElement(content)) {
            is JsonArray -> RpgObjectArray(
                json.filterIsInstance<JsonObject>().map {
                    decodeFromJsonObject(rpgObjSerializeContext, it)
                }
            )
            is JsonObject -> decodeFromJsonObject(rpgObjSerializeContext, json)
            is JsonPrimitive -> error("协议内容必须是 JsonArray 或 JsonObject")
        }
    }

    private fun decodeFromJsonObject(rpgObjSerializeContext: RpgObjSerializeContext<JsonObject>, json: JsonObject): RpgObject {
        val elementId = (json["elementId"] as? JsonPrimitive)?.content?.toLong()!!
        val data = (json["data"] as? JsonObject)
        return rpgObjSerializeContext.getRpgElement(elementId).run {
            createRpgObject(
                JsonRpgElementContext(rpgObjSerializeContext, this, data)
            )
        }
    }

}

class JsonRpgElement(
    override val id: Long,
    override val constructorId: Long,
    override val data: JsonObject?
) : RpgElement<JsonObject> {

    override fun createRpgObject(rpgElementContext: RpgElementContext<JsonObject>): RpgObject {
        return rpgElementContext.getConstructor(constructorId).construct(
            // 延迟创建 RpgObject 时，需要确保 rpgElementCenter 中已经有系统所需的所有 RpgElement
            JsonRpgObjConstructContext(rpgElementContext)
        )
    }

}

class JsonRpgElementContext(
    private val rpgObjSerializeContext: RpgObjSerializeContext<JsonObject>,
    override val current: RpgElement<JsonObject>,
    override val data: JsonObject?
) : RpgElementContext<JsonObject> {

    override fun getRpgElementOrNull(id: Long): RpgElement<JsonObject>?
            = rpgObjSerializeContext.getRpgElementOrNull(id)

    override fun getRpgObjectConstructorOrNull(id: Long): RpgObjectConstructor<JsonObject>?
            = rpgObjSerializeContext.getRpgObjConstructorOrNull(id)

}

class JsonRpgObjDeconstructContext(
    override val elementId: Long,
    private val rpgObjSerializeContext: RpgObjSerializeContext<JsonObject>,
    override val rpgObject: RpgObject,
) : RpgObjectDeconstructContext<JsonObject> {

    override fun getConstructorByElementIdOrNull(elementId: Long): RpgObjectConstructor<JsonObject>? {
        return rpgObjSerializeContext.getRpgElementOrNull(elementId)?.let {
            rpgObjSerializeContext.getRpgObjConstructorOrNull(it.constructorId)
        }
    }
}

class JsonRpgObjConstructContext(
    private val rpgElementContext: RpgElementContext<JsonObject>,
) : RpgObjectConstructContext<JsonObject> {
    override val elementId: Long
        get() = rpgElementContext.current.id

    override fun getConstructorByElementIdOrNull(elementId: Long): RpgObjectConstructor<JsonObject>? {
        return rpgElementContext.getRpgElementOrNull(elementId)?.let {
            rpgElementContext.getRpgObjectConstructorOrNull(it.constructorId)
        }
    }

    override fun getIntOrNull(key: String): Int? {
        return getIntOrNull(key, rpgElementContext.data) ?: getIntOrNull(key, rpgElementContext.current.data)
    }

    override fun getStringOrNull(key: String): String? {
        return getStringOrNull(key, rpgElementContext.data) ?: getStringOrNull(key, rpgElementContext.current.data)
    }

    override fun getDoubleOrNull(key: String): Double? {
        return getDoubleOrNull(key, rpgElementContext.data) ?: getDoubleOrNull(key, rpgElementContext.current.data)
    }

    override fun getBooleanOrNull(key: String): Boolean? {
        return getBooleanOrNull(key, rpgElementContext.data) ?: getBooleanOrNull(key, rpgElementContext.current.data)
    }

    override fun getRpgObjectOrNull(key: String): RpgObject? {
        return getRpgObjectOrNull(key, rpgElementContext.data) ?: getRpgObjectOrNull(key, rpgElementContext.current.data)
    }

    private fun getIntOrNull(key: String, param: JsonObject?): Int? {
        return (param?.get(key) as? JsonPrimitive)?.intOrNull
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

    private fun decodeToRpgObject(elementId: Long, data: JsonObject? = null): RpgObject? {
        return rpgElementContext.getRpgElementOrNull(elementId)?.run {
            createRpgObject(
                DelegateRpgElementContext(this, rpgElementContext, data)
            )
        }
    }

    private fun decodeToRpgObject(json: JsonPrimitive): RpgObject? {
        return json.longOrNull?.let(this::decodeToRpgObject)
    }

    private fun decodeToRpgObject(json: JsonObject): RpgObject? {
        return (json["elementId"] as? JsonPrimitive)?.longOrNull?.let {
            decodeToRpgObject(it, json["data"] as? JsonObject)
        }
    }

    companion object Empty : RpgObjectConstructContext<JsonObject> {
        override val elementId: Long
            get() = UNKNOWN_CONSTRUCTOR_ID
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