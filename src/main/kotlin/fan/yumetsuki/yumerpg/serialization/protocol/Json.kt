package fan.yumetsuki.yumerpg.serialization.protocol

import fan.yumetsuki.yumerpg.core.serialization.*
import fan.yumetsuki.yumerpg.serialization.*
import kotlinx.serialization.json.*

/**
 * RpgElement 反序列化协议的 JSON 实现
 * {
 *  "id": Long, // RpgElement id，必填
 *  "constructor": Long, // 构建对应的 RpgObject 的 builderId，必填
 *  "data": JsonObject? // 参数，由 builder 生成 RpgObject 时动态读取解析
 * }
 * @author yumetsuki
 */
@Suppress("MemberVisibilityCanBePrivate")
object JsonRpgElementProtocol : RpgElementProtocol<String, JsonObject> {

    const val ID = "id"

    const val CONSTRUCTOR = "constructor"

    const val DATA = "data"

    override fun decodeFromContent(content: String): RpgElement<JsonObject> {
        return when (val json = Json.parseToJsonElement(content)) {
            is JsonArray -> RpgElementArray(json.filterIsInstance<JsonObject>().map(this::decodeToRpgElement))
            is JsonObject -> decodeToRpgElement(json)
            is JsonPrimitive -> error("仅支持 JsonArray 和 JsonObject 协议")
        }
    }

    private fun decodeToRpgElement(json: JsonObject): RpgElement<JsonObject> {
        val id = (json[ID] as? JsonPrimitive)?.content?.toLong()!!
        val builderId = (json[CONSTRUCTOR] as? JsonPrimitive)?.content?.toLong()!!
        val param = (json[DATA] as? JsonObject)
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

    const val ELEMENT_ID = "elementId"

    const val DATA = "data"

    override fun encodeToContent(
        rpgObjSerializeContext: RpgObjSerializeContext<JsonObject>,
        serializable: RpgObject
    ): String {
        return when(serializable) {
            is RpgObjectArray -> buildJsonArray {
                serializable.forEach {
                    add(encodeToJsonObject(rpgObjSerializeContext, it))
                }
            }
            else -> encodeToJsonObject(rpgObjSerializeContext, serializable)
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
        val elementId = (json[ELEMENT_ID] as? JsonPrimitive)?.content?.toLong()!!
        val data = (json[DATA] as? JsonObject)
        return rpgObjSerializeContext.getRpgElement(elementId).run {
            createRpgObject(
                JsonRpgElementContext(rpgObjSerializeContext, this, data)
            )
        }
    }

    private fun encodeToJsonObject(rpgObjSerializeContext: RpgObjSerializeContext<JsonObject>, serializable: RpgObject) : JsonObject {
        return buildJsonObject {
            put(ELEMENT_ID, serializable.elementId)
            putJsonObject(DATA) {
                rpgObjSerializeContext.getRpgObjConstructor(
                    rpgObjSerializeContext.getRpgElement(serializable.elementId).constructorId
                ).deconstruct(
                    JsonRpgObjDeconstructContext(rpgObjSerializeContext, serializable, this)
                )
            }
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

    override fun getRpgObjectConstructorOrNull(id: Long): RpgObjectConstructor?
            = rpgObjSerializeContext.getRpgObjConstructorOrNull(id)

}

class JsonRpgObjDeconstructContext(
    private val rpgObjSerializeContext: RpgObjSerializeContext<JsonObject>,
    override val rpgObject: RpgObject,
    private val dataBuilder: JsonObjectBuilder
) : RpgObjectDeconstructContext {

    override val elementId: Long
        get() = rpgObject.elementId

    override fun getConstructorByElementIdOrNull(elementId: Long): RpgObjectConstructor? {
        return rpgObjSerializeContext.getRpgElementOrNull(elementId)?.let {
            rpgObjSerializeContext.getRpgObjConstructorOrNull(it.constructorId)
        }
    }

    override fun deconstruct(deconstruction: RpgObjectDataBuilder.() -> Unit) {
        JsonRpgObjectDataBuilder(dataBuilder, rpgObjSerializeContext).deconstruction()
    }

}

class JsonRpgObjectDataBuilder(
    private val jsonBuilder: JsonObjectBuilder,
    private val rpgObjSerializeContext: RpgObjSerializeContext<JsonObject>,
): RpgObjectDataBuilder {
    override fun put(key: String, value: Int) {
        jsonBuilder.put(key, value)
    }

    override fun put(key: String, value: String) {
        jsonBuilder.put(key, value)
    }

    override fun put(key: String, value: Boolean) {
        jsonBuilder.put(key, value)
    }

    override fun put(key: String, value: Double) {
        jsonBuilder.put(key, value)
    }

    override fun put(key: String, value: RpgObject) {
        when(value) {
            is RpgObjectArray -> {
                jsonBuilder.put(key, JsonArray(
                    value.map(this::buildRpgObjectJson).filter {
                        it.isNotEmpty()
                    }
                ))
            }
            else -> {
                jsonBuilder.put(key, buildRpgObjectJson(value))
            }
        }

    }

    private fun buildRpgObjectJson(value: RpgObject): JsonObject {
        return buildJsonObject {
            rpgObjSerializeContext.getRpgObjConstructorOrNullByElementId(value.elementId)?.let {
                put(JsonRpgObjectProtocol.ELEMENT_ID, value.elementId)
                putJsonObject(JsonRpgObjectProtocol.DATA) {
                    it.deconstruct(
                        JsonRpgObjDeconstructContext(rpgObjSerializeContext, value, this)
                    )
                }
            }
        }
    }

}

class JsonRpgObjConstructContext(
    private val rpgElementContext: RpgElementContext<JsonObject>,
) : RpgObjectConstructContext {
    override val elementId: Long
        get() = rpgElementContext.current.id

    override fun getConstructorByElementIdOrNull(elementId: Long): RpgObjectConstructor? {
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
        return (json[JsonRpgObjectProtocol.ELEMENT_ID] as? JsonPrimitive)?.longOrNull?.let {
            decodeToRpgObject(it, json[JsonRpgObjectProtocol.DATA] as? JsonObject)
        }
    }

    companion object Empty : RpgObjectConstructContext {
        override val elementId: Long
            get() = UNKNOWN_CONSTRUCTOR_ID
    }
}