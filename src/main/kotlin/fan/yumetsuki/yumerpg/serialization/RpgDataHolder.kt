package fan.yumetsuki.yumerpg.serialization

/**
 * Rpg 数据持有者，[RpgElement] 等会将协议中的数据持有，具体需要根据协议格式获取数据
 * @author yumetsuki
 */
interface RpgDataHolder {

    suspend fun getLongOrNull(key: String): Long? = null

    suspend fun getStringOrNull(key: String): String? = null

    suspend fun getDoubleOrNull(key: String): Double? = null

    suspend fun getBooleanOrNull(key: String): Boolean? = null

    suspend fun getRpgObjectOrNull(key: String): RpgObject? = null

    suspend fun getSubDataOrNull(key: String): RpgDataHolder? = null

    suspend fun getSubArrayOrNull(key: String): RpgArrayDataHolder? = null

    suspend fun forEach(func: suspend (Pair<String, Any>) -> Unit) = Unit
}

interface RpgArrayDataHolder : Iterable<Any> {

    fun getLongOrNull(index: Int): Long? = null

    fun getStringOrNull(index: Int): String? = null

    fun getDoubleOrNull(index: Int): Double? = null

    fun getBooleanOrNull(index: Int): Boolean? = null

    fun getRpgObjectOrNull(index: Int): RpgObject? = null

    fun getSubDataOrNull(index: Int): RpgDataHolder? = null

    fun getSubArrayOrNull(index: Int): RpgArrayDataHolder? = null

}

suspend fun RpgDataHolder.getLong(key: String): Long = getLongOrNull(key) ?: error("int $key 不存在")
suspend fun RpgDataHolder.getString(key: String): String = getStringOrNull(key) ?: error("string $key 不存在")
suspend fun RpgDataHolder.getDouble(key: String): Double = getDoubleOrNull(key) ?: error("double $key 不存在")
suspend fun RpgDataHolder.getBoolean(key: String): Boolean = getBooleanOrNull(key) ?: error("boolean $key 不存在")
suspend fun RpgDataHolder.getRpgObject(key: String): RpgObject = getRpgObjectOrNull(key) ?: error("RpgObject $key 不存在")

suspend inline fun <reified T> RpgDataHolder.getOrNull(key: String): T? {
    return when(T::class) {
        Int::class -> getLongOrNull(key)?.toInt() as T
        Long::class -> getLongOrNull(key) as T
        Double::class -> getDoubleOrNull(key) as T
        Boolean::class -> getBooleanOrNull(key) as T
        String::class -> getStringOrNull(key) as T
        RpgObject::class -> getRpgObjectOrNull(key) as T
        else -> error("不支持的数据类型 ${T::class}")
    }
}

suspend inline fun <reified T> RpgDataHolder.get(key: String): T {
    return getOrNull<T>(key)!!
}
