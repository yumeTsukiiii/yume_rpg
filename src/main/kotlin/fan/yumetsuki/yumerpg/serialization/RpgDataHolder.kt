package fan.yumetsuki.yumerpg.serialization

/**
 * Rpg 数据持有者，[RpgElement] 等会将协议中的数据持有，具体需要根据协议格式获取数据
 * @author yumetsuki
 */
interface RpgDataHolder {

    fun getIntOrNull(key: String): Int? = null

    fun getStringOrNull(key: String): String? = null

    fun getDoubleOrNull(key: String): Double? = null

    fun getBooleanOrNull(key: String): Boolean? = null

    fun getRpgObjectOrNull(key: String): RpgObject? = null

}

fun RpgDataHolder.getInt(key: String): Int = getIntOrNull(key) ?: error("int $key 不存在")
fun RpgDataHolder.getString(key: String): String = getStringOrNull(key) ?: error("string $key 不存在")
fun RpgDataHolder.getDouble(key: String): Double = getDoubleOrNull(key) ?: error("double $key 不存在")
fun RpgDataHolder.getBoolean(key: String): Boolean = getBooleanOrNull(key) ?: error("boolean $key 不存在")
fun RpgDataHolder.getRpgObject(key: String): RpgObject = getRpgObjectOrNull(key) ?: error("RpgObject $key 不存在")