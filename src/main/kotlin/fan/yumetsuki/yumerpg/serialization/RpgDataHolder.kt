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