package fan.yumetsuki.yumerpg.serialization.protocol

import fan.yumetsuki.yumerpg.serialization.*
import kotlinx.serialization.json.*

object JsonByteElementProtocol : RpgElementProtocol<ByteArray> {
    override fun decodeFromContent(content: ByteArray): RpgElement {
        return JsonRpgElementProtocol.decodeFromContent(content.decodeToString())
    }

}

object JsonByteObjectProtocol : RpgObjectProtocol<ByteArray> {

    override fun encodeToContent(rpgObjSerializeContext: RpgObjSerializeContext, serializable: RpgObject): ByteArray {
        return JsonRpgObjectProtocol.encodeToContent(rpgObjSerializeContext, serializable).encodeToByteArray()
    }

    override fun decodeFromContent(rpgObjSerializeContext: RpgObjSerializeContext, content: ByteArray): RpgObject {
        return JsonRpgObjectProtocol.decodeFromContent(rpgObjSerializeContext, content.decodeToString())
    }

}

/**
 * RpgElement 反序列化协议的 JSON 实现
 * {
 *  "id": Long?, // RpgElement id，可选，当 id 为 null 时，使用 name 生成 id
 *  "name": String? // 可选，当 id 为 null 时，使用 name 生成 id
 *  "constructor": Long|String, // 构建对应的 RpgObject 的 Constructor id or Constructor Name，必填
 *  "data": JsonObject? // 参数，由 builder 生成 RpgObject 时动态读取解析
 * }
 * @author yumetsuki
 */
@Suppress("MemberVisibilityCanBePrivate")
object JsonRpgElementProtocol : RpgElementProtocol<String> {

    const val ID = "id"

    const val NAME = "name"

    const val CONSTRUCTOR = "constructor"

    const val DATA = "data"

    override fun decodeFromContent(content: String): RpgElement {
        return when (val json = Json.parseToJsonElement(content)) {
            is JsonArray -> RpgElementArray(json.filterIsInstance<JsonObject>().map(this::decodeToRpgElement))
            is JsonObject -> decodeToRpgElement(json)
            is JsonPrimitive -> error("仅支持 JsonArray 和 JsonObject 协议")
        }
    }

    private fun decodeToRpgElement(json: JsonObject): RpgElement {
        val id = (json[ID] as? JsonPrimitive)?.content?.toLongOrNull() ?: (json[NAME] as? JsonPrimitive)?.content?.let {
            RpgElement.getId(it)
        }!!
        val constructor = (json[CONSTRUCTOR] as? JsonPrimitive)?.content!!
        val constructorId = constructor.toLongOrNull() ?: RpgObjectConstructor.getId(constructor)
        val param = (json[DATA] as? JsonObject)
        return JsonRpgElement(id, constructorId, param)
    }

}

/**
 * RpgObject 序列化、反序列化协议的 JSON 实现
 * {
 *  "element": Long|String, // RpgObject 对应创建者 RpgElement id 或者 name，必填
 *  "data": JsonObject? // 存储参数，由 builder 生成 RpgObject 时动态读取解析，并在[encodeToContent]时存储，当无存档生成对象时，将由[RpgElement]中的参数填充
 * }
 * [RpgObject] 会由其对应的 [RpgElement] 创建，因此该协议必须依赖构建好的 Element 列表以及注册的 [RpgObjectConstructor] 列表
 * @author yumetsuki
 */
object JsonRpgObjectProtocol: RpgObjectProtocol<String> {

    const val ELEMENT = "element"

    const val DATA = "data"

    override fun encodeToContent(
        rpgObjSerializeContext: RpgObjSerializeContext,
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
        rpgObjSerializeContext: RpgObjSerializeContext,
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

    private fun decodeFromJsonObject(rpgObjSerializeContext: RpgObjSerializeContext, json: JsonObject): RpgObject {
        val element = (json[ELEMENT] as? JsonPrimitive)?.content!!
        val elementId = element.toLongOrNull() ?: RpgElement.getId(element)
        val data = (json[DATA] as? JsonObject)
        return rpgObjSerializeContext.getRpgElement(elementId).run {
            createRpgObject(
                JsonRpgElementContext(rpgObjSerializeContext, this, JsonRpgDataHolder(data))
            )
        }
    }

    private fun encodeToJsonObject(rpgObjSerializeContext: RpgObjSerializeContext, serializable: RpgObject) : JsonObject {
        return buildJsonObject {
            put(ELEMENT, serializable.elementId)
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

class JsonRpgDataHolder(
    private val data: JsonObject? = null
) : RpgDataHolder {

    override fun getLongOrNull(key: String): Long? {
        return (data?.get(key) as? JsonPrimitive)?.longOrNull
    }

    override fun getStringOrNull(key: String): String? {
        return data?.get(key)?.let {
            when(it) {
                is JsonPrimitive -> it.contentOrNull
                else -> it.toString()
            }
        }
    }

    override fun getDoubleOrNull(key: String): Double? {
        return (data?.get(key) as? JsonPrimitive)?.doubleOrNull
    }

    override fun getBooleanOrNull(key: String): Boolean? {
        return (data?.get(key) as? JsonPrimitive)?.booleanOrNull
    }

    override fun getSubDataOrNull(key: String): RpgDataHolder? {
        return (data?.get(key) as? JsonObject)?.let {
            JsonRpgDataHolder(it)
        }
    }

    override fun getSubArrayOrNull(key: String): RpgArrayDataHolder? {
        return (data?.get(key) as? JsonArray)?.let {
            JsonRpgArrayDataHolder(it)
        }
    }
}

class JsonRpgArrayDataHolder(
    private val data: JsonArray? = null
) : RpgArrayDataHolder {

    override fun getLongOrNull(index: Int): Long? {
        return (data?.get(index) as? JsonPrimitive)?.longOrNull
    }

    override fun getStringOrNull(index: Int): String? {
        return (data?.get(index) as? JsonPrimitive)?.contentOrNull
    }

    override fun getDoubleOrNull(index: Int): Double? {
        return (data?.get(index) as? JsonPrimitive)?.doubleOrNull
    }

    override fun getBooleanOrNull(index: Int): Boolean? {
        return (data?.get(index) as? JsonPrimitive)?.booleanOrNull
    }

    override fun getSubDataOrNull(index: Int): RpgDataHolder? {
        return (data?.get(index) as? JsonObject)?.let {
            JsonRpgDataHolder(it)
        }
    }

    override fun getSubArrayOrNull(index: Int): RpgArrayDataHolder? {
        return (data?.get(index) as? JsonArray)?.let {
            JsonRpgArrayDataHolder(it)
        }
    }

    override fun iterator(): Iterator<Any> = data?.iterator() ?: emptyList<Any>().iterator()
}

class JsonRpgElement(
    override val id: Long,
    override val constructorId: Long,
    data: JsonObject?
) : RpgElement, RpgDataHolder by JsonRpgDataHolder(data) {

    override fun createRpgObject(rpgElementContext: RpgElementContext): RpgObject {
        return rpgElementContext.getConstructor(constructorId).construct(
            // 延迟创建 RpgObject 时，需要确保 rpgElementCenter 中已经有系统所需的所有 RpgElement
            JsonRpgObjConstructContext(rpgElementContext)
        )
    }

}

class JsonRpgElementContext(
    private val rpgObjSerializeContext: RpgObjSerializeContext,
    override val current: RpgElement,
    private val rpgDataHolder: RpgDataHolder?
) : RpgElementContext {

    override fun getRpgElementOrNull(id: Long): RpgElement?
            = rpgObjSerializeContext.getRpgElementOrNull(id)

    override fun getRpgObjectConstructorOrNull(id: Long): RpgObjectConstructor?
            = rpgObjSerializeContext.getRpgObjConstructorOrNull(id)

    override fun getLongOrNull(key: String): Long? {
        return rpgDataHolder?.getLongOrNull(key) ?: current.getLongOrNull(key)
    }

    override fun getStringOrNull(key: String): String? {
        return rpgDataHolder?.getStringOrNull(key) ?: current.getStringOrNull(key)
    }

    override fun getDoubleOrNull(key: String): Double? {
        return rpgDataHolder?.getDoubleOrNull(key) ?: current.getDoubleOrNull(key)
    }

    override fun getBooleanOrNull(key: String): Boolean? {
        return rpgDataHolder?.getBooleanOrNull(key) ?: current.getBooleanOrNull(key)
    }

    override fun getRpgObjectOrNull(key: String): RpgObject? {
        return getRpgObjectOrNull(key, rpgDataHolder) ?: getRpgObjectOrNull(key, current)
    }

    override fun getSubDataOrNull(key: String): RpgDataHolder? {
        return rpgDataHolder?.getSubDataOrNull(key) ?: current.getSubDataOrNull(key)
    }

    private fun getRpgObjectOrNull(key: String, rpgDataHolder: RpgDataHolder?): RpgObject? {
        return rpgDataHolder?.getLongOrNull(key)?.let {
            // 1. 从 elementId 中生成
            decodeToRpgObject(it)
        } ?: rpgDataHolder?.getStringOrNull(key)?.let {
            // 2. 从 elementName 中生成
            decodeToRpgObject(RpgElement.getId(it))
        } ?: rpgDataHolder?.getSubDataOrNull(key)?.let {
            // 3. 从 { element: Long|String, data: JsonObject? } 中生成
            val elementId = it.getLongOrNull(JsonRpgObjectProtocol.ELEMENT)
            val data = it.getSubDataOrNull(JsonRpgObjectProtocol.DATA)
            if (elementId != null) {
                getRpgElementOrNull(elementId)?.run {
                    createRpgObject(
                        JsonRpgElementContext(
                            rpgObjSerializeContext, this, data
                        )
                    )
                }
            } else {
                null
            }
        } ?: rpgDataHolder?.getSubArrayOrNull(key)?.let {
            // 4. 从 JsonArray 中生成
            RpgObjectArray(
                it.mapNotNull { item ->
                    when (item) {
                        // 4.1 从 [JsonObject, JsonObject] 中生成
                        is JsonObject -> decodeToRpgObject(item)
                        // 4.2 从 [elementId|elementName, elementId|elementName] 中生成
                        is JsonPrimitive -> decodeToRpgObject(item)
                        // 4.3 从 [elementId, elementId] 中生成
                        is Int -> decodeToRpgObject(item.toLong())
                        // 4.3 从 [elementId, elementId] 中生成
                        is Long -> decodeToRpgObject(item)
                        // 4.4 从 [elementName, elementName] 中生成
                        is String -> decodeToRpgObject(RpgElement.getId(item))
                        else -> null
                    }
                }
            )
        }
    }

    private fun decodeToRpgObject(elementId: Long, data: JsonObject? = null): RpgObject? {
        return getRpgElementOrNull(elementId)?.run {
            createRpgObject(
                JsonRpgElementContext(
                    rpgObjSerializeContext, this, JsonRpgDataHolder(data)
                )
            )
        }
    }

    private fun decodeToRpgObject(json: JsonPrimitive): RpgObject? {
        return json.longOrNull?.let(this::decodeToRpgObject) ?: json.contentOrNull?.let { decodeToRpgObject(RpgElement.getId(it)) }
    }

    private fun decodeToRpgObject(json: JsonObject): RpgObject? {
        return (json[JsonRpgObjectProtocol.ELEMENT] as? JsonPrimitive)?.longOrNull?.let {
            decodeToRpgObject(it, json[JsonRpgObjectProtocol.DATA] as? JsonObject)
        }
    }
}

class JsonRpgObjDeconstructContext(
    private val rpgObjSerializeContext: RpgObjSerializeContext,
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
    private val rpgObjSerializeContext: RpgObjSerializeContext,
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
                put(JsonRpgObjectProtocol.ELEMENT, value.elementId)
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
    private val rpgElementContext: RpgElementContext,
) : RpgObjectConstructContext, RpgDataHolder by rpgElementContext {
    override val elementId: Long
        get() = rpgElementContext.current.id

    override fun getConstructorByElementIdOrNull(elementId: Long): RpgObjectConstructor? {
        return rpgElementContext.getRpgElementOrNull(elementId)?.let {
            rpgElementContext.getRpgObjectConstructorOrNull(it.constructorId)
        }
    }

    companion object Empty : RpgObjectConstructContext {
        override val elementId: Long
            get() = UNKNOWN_CONSTRUCTOR_ID
    }
}