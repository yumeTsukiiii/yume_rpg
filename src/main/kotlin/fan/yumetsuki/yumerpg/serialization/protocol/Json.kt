package fan.yumetsuki.yumerpg.serialization.protocol

import fan.yumetsuki.yumerpg.serialization.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*

object JsonByteElementProtocol : RpgElementProtocol<ByteArray> {
    override fun decodeFromContent(content: ByteArray): RpgElement {
        return JsonRpgElementProtocol.decodeFromContent(content.decodeToString())
    }

}

object JsonByteObjectProtocol : RpgObjectProtocol<ByteArray> {

    override suspend fun encodeToContent(rpgObjSerializeContext: RpgObjSerializeContext, serializable: RpgObject): ByteArray {
        return JsonRpgObjectProtocol.encodeToContent(rpgObjSerializeContext, serializable).encodeToByteArray()
    }

    override suspend fun decodeFromContent(rpgObjSerializeContext: RpgObjSerializeContext, content: ByteArray): RpgObject {
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

    override suspend fun encodeToContent(
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

    override suspend fun decodeFromContent(
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

    private suspend fun decodeFromJsonObject(rpgObjSerializeContext: RpgObjSerializeContext, json: JsonObject): RpgObject {
        val element = (json[ELEMENT] as? JsonPrimitive)?.content!!
        val elementId = element.toLongOrNull() ?: RpgElement.getId(element)
        val data = (json[DATA] as? JsonObject)
        return rpgObjSerializeContext.getRpgElement(elementId).run {
            createRpgObject(
                JsonRpgElementContext(rpgObjSerializeContext, this, JsonRpgDataHolder(data))
            )
        }
    }

    private suspend fun encodeToJsonObject(rpgObjSerializeContext: RpgObjSerializeContext, serializable: RpgObject) : JsonObject {
        return buildJsonObject {
            put(ELEMENT, serializable.elementId)
            put(DATA, buildJsonObject {
                rpgObjSerializeContext.getRpgObjConstructor(
                    rpgObjSerializeContext.getRpgElement(serializable.elementId).constructorId
                ).deconstruct(
                    JsonRpgObjDeconstructContext(rpgObjSerializeContext, serializable, this)
                )
            })
        }
    }

}

class DelegateRpgDataHolder(
    private val rpgDataHolder: RpgDataHolder? = null,
    private val default: RpgDataHolder? = null
): RpgDataHolder {

    override suspend fun getLongOrNull(key: String): Long? {
        return rpgDataHolder?.getLongOrNull(key) ?: default?.getLongOrNull(key)
    }

    override suspend fun getStringOrNull(key: String): String? {
        return rpgDataHolder?.getStringOrNull(key) ?: default?.getStringOrNull(key)
    }

    override suspend fun getDoubleOrNull(key: String): Double? {
        return rpgDataHolder?.getDoubleOrNull(key) ?: default?.getDoubleOrNull(key)
    }

    override suspend fun getBooleanOrNull(key: String): Boolean? {
        return rpgDataHolder?.getBooleanOrNull(key) ?: default?.getBooleanOrNull(key)
    }

    override suspend fun getRpgObjectOrNull(key: String): RpgObject? {
        return rpgDataHolder?.getRpgObjectOrNull(key) ?: default?.getRpgObjectOrNull(key)
    }

    override suspend fun getSubDataOrNull(key: String): RpgDataHolder? {
        return rpgDataHolder?.getSubDataOrNull(key) ?: default?.getSubDataOrNull(key)
    }

    override suspend fun getSubArrayOrNull(key: String): RpgArrayDataHolder? {
        return rpgDataHolder?.getSubArrayOrNull(key) ?: default?.getSubArrayOrNull(key)
    }

    // TODO 考虑 default
    override suspend fun forEach(func: suspend (Pair<String, Any>) -> Unit) {
        rpgDataHolder?.forEach(func)
    }
}

class DelegateRpgArrayDataHolder(
    private val rpgArrayDataHolder: RpgArrayDataHolder? = null,
    private val default: RpgArrayDataHolder? = null
) : RpgArrayDataHolder {
    override fun getLongOrNull(index: Int): Long? {
        return rpgArrayDataHolder?.getLongOrNull(index) ?: default?.getLongOrNull(index)
    }

    override fun getStringOrNull(index: Int): String? {
        return rpgArrayDataHolder?.getStringOrNull(index) ?: default?.getStringOrNull(index)
    }

    override fun getDoubleOrNull(index: Int): Double? {
        return rpgArrayDataHolder?.getDoubleOrNull(index) ?: default?.getDoubleOrNull(index)
    }

    override fun getBooleanOrNull(index: Int): Boolean? {
        return rpgArrayDataHolder?.getBooleanOrNull(index) ?: default?.getBooleanOrNull(index)
    }

    override fun getRpgObjectOrNull(index: Int): RpgObject? {
        return rpgArrayDataHolder?.getRpgObjectOrNull(index) ?: default?.getRpgObjectOrNull(index)
    }

    override fun getSubDataOrNull(index: Int): RpgDataHolder? {
        return rpgArrayDataHolder?.getSubDataOrNull(index) ?: default?.getSubDataOrNull(index)
    }

    override fun getSubArrayOrNull(index: Int): RpgArrayDataHolder? {
        return rpgArrayDataHolder?.getSubArrayOrNull(index) ?: default?.getSubArrayOrNull(index)
    }

    override fun iterator(): Iterator<Any> {
        return rpgArrayDataHolder?.iterator() ?: emptyList<Any>().iterator()
    }
}

class JsonRpgDataHolder(
    private val data: JsonObject? = null,
    private val default: RpgDataHolder? = null
) : RpgDataHolder {

    override suspend fun getLongOrNull(key: String): Long? {
        return (data?.get(key) as? JsonPrimitive)?.longOrNull ?: default?.getLongOrNull(key)
    }

    override suspend fun getStringOrNull(key: String): String? {
        return data?.get(key)?.let {
            when(it) {
                is JsonPrimitive -> it.contentOrNull
                else -> it.toString()
            }
        } ?: default?.getStringOrNull(key)
    }

    override suspend fun getDoubleOrNull(key: String): Double? {
        return (data?.get(key) as? JsonPrimitive)?.doubleOrNull ?: default?.getDoubleOrNull(key)
    }

    override suspend fun getBooleanOrNull(key: String): Boolean? {
        return (data?.get(key) as? JsonPrimitive)?.booleanOrNull ?: default?.getBooleanOrNull(key)
    }

    override suspend fun getSubDataOrNull(key: String): RpgDataHolder? {
        return (data?.get(key) as? JsonObject)?.let {
            JsonRpgDataHolder(it, default?.getSubDataOrNull(key))
        }
    }

    override suspend fun getSubArrayOrNull(key: String): RpgArrayDataHolder? {
        return (data?.get(key) as? JsonArray)?.let {
            JsonRpgArrayDataHolder(it, default?.getSubArrayOrNull(key))
        }
    }

    // TODO 重写成 iterator 支持掉 default
    override suspend fun forEach(func: suspend (Pair<String, Any>) -> Unit) {
        data?.forEach { (k, v) ->
            func(k to v.run {
                when(this) {
                    is JsonPrimitive -> {
                        this.doubleOrNull ?: this.longOrNull ?: this.booleanOrNull ?: this.content
                    }
                    is JsonObject -> JsonRpgDataHolder(this, default?.getSubDataOrNull(k))
                    is JsonArray -> JsonRpgArrayDataHolder(this, default?.getSubArrayOrNull(k))
                }
            })
        }
    }
}

class JsonRpgArrayDataHolder(
    private val data: JsonArray? = null,
    private val default: RpgArrayDataHolder? = null
) : RpgArrayDataHolder {

    override fun getLongOrNull(index: Int): Long? {
        return (data?.getOrNull(index) as? JsonPrimitive)?.longOrNull ?: default?.getLongOrNull(index)
    }

    override fun getStringOrNull(index: Int): String? {
        return (data?.getOrNull(index) as? JsonPrimitive)?.contentOrNull ?: default?.getStringOrNull(index)
    }

    override fun getDoubleOrNull(index: Int): Double? {
        return (data?.getOrNull(index) as? JsonPrimitive)?.doubleOrNull ?: default?.getDoubleOrNull(index)
    }

    override fun getBooleanOrNull(index: Int): Boolean? {
        return (data?.getOrNull(index) as? JsonPrimitive)?.booleanOrNull ?: default?.getBooleanOrNull(index)
    }

    override fun getSubDataOrNull(index: Int): RpgDataHolder? {
        return (data?.getOrNull(index) as? JsonObject)?.let {
            JsonRpgDataHolder(it, default?.getSubDataOrNull(index))
        }
    }

    override fun getSubArrayOrNull(index: Int): RpgArrayDataHolder? {
        return (data?.getOrNull(index) as? JsonArray)?.let {
            JsonRpgArrayDataHolder(it, default?.getSubArrayOrNull(index))
        }
    }

    // TODO 重写 iterator 支持掉 default
    override fun iterator(): Iterator<Any> = data?.iterator() ?: emptyList<Any>().iterator()
}

class JsonRpgElement(
    override val id: Long,
    override val constructorId: Long,
    data: JsonObject?
) : RpgElement, RpgDataHolder by JsonRpgDataHolder(data) {

    override suspend fun createRpgObject(rpgElementContext: RpgElementContext): RpgObject {
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

    override suspend fun getLongOrNull(key: String): Long? {
        return rpgDataHolder?.getLongOrNull(key) ?: current.getLongOrNull(key)
    }

    override suspend fun getStringOrNull(key: String): String? {
        return rpgDataHolder?.getStringOrNull(key) ?: current.getStringOrNull(key)
    }

    override suspend fun getDoubleOrNull(key: String): Double? {
        return rpgDataHolder?.getDoubleOrNull(key) ?: current.getDoubleOrNull(key)
    }

    override suspend fun getBooleanOrNull(key: String): Boolean? {
        return rpgDataHolder?.getBooleanOrNull(key) ?: current.getBooleanOrNull(key)
    }

    override suspend fun getRpgObjectOrNull(key: String): RpgObject? {
        return createRpgObjectOrNull(key, rpgDataHolder) ?: createRpgObjectOrNull(key, current)
    }

    override suspend fun getSubDataOrNull(key: String): RpgDataHolder? {
        return rpgDataHolder?.getSubDataOrNull(key)?.let {
            JsonRpgElementContext(rpgObjSerializeContext, current, it)
        } ?: current.getSubDataOrNull(key)?.let {
            JsonRpgElementContext(rpgObjSerializeContext, current, it)
        }
    }

    override suspend fun forEach(func: suspend (Pair<String, Any>) -> Unit) {
        val keys = hashSetOf<String>()
        rpgDataHolder?.forEach {
            keys.add(it.first)
            func(it)
        }
        // RpgObject 数据中已有的，覆盖 element 中的数据，不再遍历
        current.forEach {
            if (it.first !in keys) {
                func(it)
            }
        }
    }

    private suspend fun createRpgObjectOrNull(key: String, rpgDataHolder: RpgDataHolder?): RpgObject? {
        return rpgDataHolder?.getLongOrNull(key)?.let {
            // 1. 从 elementId 中生成
            // 此时尝试默认从 element 的 "key": { element: Long|String, data: JsonObject? } 中获取构建数据
            decodeToRpgObject(key, it)
        } ?: rpgDataHolder?.getStringOrNull(key)?.let {
            // 2. 从 elementName 中生成,
            decodeToRpgObject(key, RpgElement.getId(it))
        } ?: rpgDataHolder?.getSubDataOrNull(key)?.let {
            // 3. 从 { element: Long|String, data: JsonObject? } 中生成
            it.getLongOrNull(JsonRpgObjectProtocol.ELEMENT)?.let { elementId ->
                decodeDataHolderToRpgObject(key, elementId, it.getSubDataOrNull(JsonRpgObjectProtocol.DATA))
            }
        } ?: rpgDataHolder?.getSubArrayOrNull(key)?.let {
            // 4. 从 JsonArray 中生成
            RpgObjectArray(
                it.mapIndexedNotNull { index, item ->
                    when (item) {
                        // 4.1 从 [JsonObject, JsonObject] 中生成
                        is JsonObject -> decodeToRpgObject(key, item, index)
                        // 4.2 从 [elementId|elementName, elementId|elementName] 中生成
                        is JsonPrimitive -> decodeToRpgObject(key, item, index)
                        // 4.3 从 [elementId, elementId] 中生成
                        is Int -> decodeToRpgObject(key, item.toLong(), index = index)
                        // 4.3 从 [elementId, elementId] 中生成
                        is Long -> decodeToRpgObject(key, item, index = index)
                        // 4.4 从 [elementName, elementName] 中生成
                        is String -> decodeToRpgObject(key, RpgElement.getId(item), index = index)
                        else -> null
                    }
                }
            )
        }
    }

    private suspend fun decodeToRpgObject(key: String, elementId: Long, data: JsonObject? = null, index: Int = -1): RpgObject? {
        return decodeDataHolderToRpgObject(key, elementId, JsonRpgDataHolder(data), index)
    }

    private suspend fun decodeDataHolderToRpgObject(key: String, elementId: Long, rpgDataHolder: RpgDataHolder?, index: Int = -1): RpgObject? {
        return getRpgElementOrNull(elementId)?.run {
            createRpgObject(
                JsonRpgElementContext(
                    rpgObjSerializeContext, this, if (index < 0) {
                        DelegateRpgDataHolder(rpgDataHolder, getElementSubData(key))
                    } else {
                        DelegateRpgDataHolder(rpgDataHolder, getElementSubArrayData(key, index))
                    }
                )
            )
        }
    }

    private suspend fun decodeToRpgObject(key: String, json: JsonPrimitive, index: Int = -1): RpgObject? {
        return json.longOrNull?.let {
            decodeToRpgObject(key, it, index = index)
        } ?: json.contentOrNull?.let {
            decodeToRpgObject(key, RpgElement.getId(it), index = index)
        }
    }

    private suspend fun decodeToRpgObject(key: String, json: JsonObject, index: Int = -1): RpgObject? {
        return (json[JsonRpgObjectProtocol.ELEMENT] as? JsonPrimitive)?.longOrNull?.let {
            decodeToRpgObject(key, it, json[JsonRpgObjectProtocol.DATA] as? JsonObject, index)
        }
    }

    private suspend fun getElementSubData(key: String): RpgDataHolder? {
        return current.getSubDataOrNull(key)?.getSubDataOrNull(JsonRpgObjectProtocol.DATA)
    }

    private suspend fun getElementSubArrayData(key: String, index: Int): RpgDataHolder? {
        return current.getSubArrayOrNull(key)?.getSubDataOrNull(index)?.getSubDataOrNull(JsonRpgObjectProtocol.DATA)
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

    override suspend fun deconstruct(deconstruction: suspend RpgObjectDataBuilder.() -> Unit) {
        JsonRpgObjectDataBuilder(dataBuilder, rpgObjSerializeContext).deconstruction()
    }

}

class JsonRpgObjectDataBuilder(
    private val jsonBuilder: JsonObjectBuilder,
    private val rpgObjSerializeContext: RpgObjSerializeContext,
): RpgObjectDataBuilder {
    override suspend fun put(key: String, value: Number) {
        jsonBuilder.put(key, value)
    }

    override suspend fun put(key: String, value: String) {
        jsonBuilder.put(key, value)
    }

    override suspend fun put(key: String, value: Boolean) {
        jsonBuilder.put(key, value)
    }

    override suspend fun put(key: String, value: RpgObject) {
        when(value) {
            is RpgObjectArray -> {
                jsonBuilder.put(key, JsonArray(
                    value.map { buildRpgObjectJson(it) }.filter {
                        it.isNotEmpty()
                    }
                ))
            }
            else -> {
                jsonBuilder.put(key, buildRpgObjectJson(value))
            }
        }

    }

    override suspend fun put(key: String, value: Map<String, Any>) {
        jsonBuilder.put(key, buildJsonObject(value))
    }

    override suspend fun put(key: String, value: List<Any>) {
        jsonBuilder.put(key, buildJsonArray(value))
    }

    override suspend fun put(key: String, value: RpgDataHolder) {
        jsonBuilder.put(key, buildJsonObject(value))
    }

    override suspend fun put(key: String, value: RpgArrayDataHolder) {
        jsonBuilder.put(key, buildJsonArray(value))
    }

    private suspend fun buildJsonObject(value: RpgDataHolder): JsonObject {
        return buildJsonObject {
            value.forEach { (k, v) ->
                when(v) {
                    is Number -> put(k, v)
                    is Boolean -> put(k, v)
                    is String -> put(k, v)
                    is RpgDataHolder -> put(k, buildJsonObject(v))
                    is RpgArrayDataHolder -> put(k, buildJsonArray(v))
                }
            }
        }
    }

    private suspend fun buildJsonObject(value: Map<String, Any>): JsonObject {
        return buildJsonObject {
            value.forEach { (k, v) ->
                when(v) {
                    is Number -> put(k, v)
                    is Boolean -> put(k, v)
                    is String -> put(k, v)
                    is RpgObject -> put(k, buildRpgObjectJson(v))
                    is RpgDataHolder -> put(k, buildJsonObject(v))
                    is RpgArrayDataHolder -> put(k, buildJsonArray(v))
                }
            }
        }
    }

    private suspend fun buildJsonArray(value: Iterable<Any>): JsonArray {
        return buildJsonArray {
            value.forEach {
                when(it) {
                    is Number -> add(it)
                    is Boolean -> add(it)
                    is String -> add(it)
                    is RpgObject -> add(buildRpgObjectJson(it))
                    is RpgDataHolder -> add(buildJsonObject(it))
                    is RpgArrayDataHolder -> add(buildJsonArray(it))
                }
            }
        }
    }

    private suspend fun buildRpgObjectJson(value: RpgObject): JsonObject {
        return buildJsonObject {
            rpgObjSerializeContext.getRpgObjConstructorOrNullByElementId(value.elementId)?.let {
                put(JsonRpgObjectProtocol.ELEMENT, value.elementId)
                put(JsonRpgObjectProtocol.DATA, buildJsonObject {
                    it.deconstruct(
                        JsonRpgObjDeconstructContext(rpgObjSerializeContext, value, this)
                    )
                })
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