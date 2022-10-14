package fan.yumetsuki.yumerpg.serialization

const val UNKNOWN_RPG_OBJECT_ID = Long.MIN_VALUE

/**
 * 游戏对象，可被序列化和反序列化
 */
interface RpgObject {
    /**
     * [RpgElement] id，表示是由哪一种类型元素构建过来的
     */
    val elementId: Long

    companion object Empty: RpgObject {
        override val elementId: Long
            get() = UNKNOWN_ELEMENT_ID
    }

}

/**
 * RpgObject 数组
 */
class RpgObjectArray(
    private val content: List<RpgObject>
) : RpgObject, List<RpgObject> by content {

    override val elementId: Long
        get() = UNKNOWN_RPG_OBJECT_ID

    override fun equals(other: Any?): Boolean = content == other
    override fun hashCode(): Int = content.hashCode()
    override fun toString(): String = content.joinToString(prefix = "[", postfix = "]", separator = ",")

}