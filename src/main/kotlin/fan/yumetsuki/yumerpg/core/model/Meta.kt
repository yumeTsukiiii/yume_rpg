package fan.yumetsuki.yumerpg.core.model

/**
 * 游戏元信息，通过 kv 的方式存储，例如游戏人物会存储 name 信息
 * @author yumetsuki
 */
interface RpgMeta {

    /**
     * 获取元信息
     * @param key 元信息的存储 key
     * @return 返回 key 对应的元数据
     */
    fun <T> get(key: String): T?

    /**
     * 存储元信息
     * @param key 元信息的存储 key
     * @param value 存储在 key 位置对应的元数据
     */
    fun set(key: String, value: Any)

    /**
     * 获取所有元信息
     * @return 元信息的 kv 键值对
     */
    fun all(): Map<String, Any>

    /**
     * 获取所有元信息的 key
     * @return 所有元信息的[String]key
     */
    fun allKey(): List<String>
}

@Suppress("UNCHECKED_CAST")
class MapRpgMeta(
    vararg data: Pair<String, Any>
): RpgMeta {

    init {
        data.forEach { (key, value) ->
            map[key] = value
        }
    }

    private val map: MutableMap<String, Any> = mutableMapOf()

    override fun <T> get(key: String): T? = map[key] as T

    override fun set(key: String, value: Any) {
        map[key] = value
    }

    override fun all(): Map<String, Any> = map

    override fun allKey(): List<String> = map.keys.toList()
}

fun mapMeta(vararg data: Pair<String, Any>) = MapRpgMeta(*data)