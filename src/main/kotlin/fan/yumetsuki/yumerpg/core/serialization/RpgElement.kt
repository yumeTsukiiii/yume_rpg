package fan.yumetsuki.yumerpg.core.serialization

/**
 * 游戏元素，它是一个被预定义的游戏中对象类型，例如，游戏中存在「HP 药水」这种道具
 * 在程序中，它是一个元素（类别），一个角色有很多个「HP 药水」，则这些为游戏中实际存在的对象[RpgObject]
 */
interface RpgElement {

    val id: Long

    fun createElement(): RpgObject

}

/**
 * 游戏对象构建器，用于解析参数列表构建指定的对象
 * 可能构建出[RpgObject]
 * @author yumetsuki
 */
interface RpgObjectBuilder {

    /**
     * 构建器的 id，用于构建系统全局管理
     */
    val id: Long

    /**
     * 构建一个[RpgObject] 对象
     * @return [RpgObject] 可能是游戏中的任何对象
     */
    fun build(buildObject: RpgObjectBuildContext?): RpgObject

}

/**
 * 构建器构建时上下文，它封装了单个原始数据对象的协议，用来获取构建时的各种信息
 * @author yumetsuki
 */
interface RpgObjectBuildContext {

    fun getIntOrNull(key: String): Int?

    fun getStringOrNull(key: String): String?

    fun getDoubleOrNull(key: String): Double?

    fun getBooleanOrNull(key: String): Boolean?

    fun getRpgObjectOrNull(key: String): RpgObject?

}

interface RpgElementCenter {

    fun registerElement(id: Long, element: RpgElement)

    fun getElement(id: Long, element: RpgElement)

}

class CommonRpgElement(
    override val id: Long,
    val builder: RpgObjectBuilder
) : RpgElement {
    override fun createElement(): RpgObject {
        TODO("Not yet implemented")
    }
}
