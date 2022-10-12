package fan.yumetsuki.yumerpg.core.serialization

/**
 * [RpgElement] 中心，集中管理所有注册在系统中的 RpgElement
 * 由系统解析数据，生成[RpgElement]注册在该 Center 中[MutableRpgElementCenter]
 * @author yumetsuki
 */
interface RpgElementCenter {

    fun getElementOrNull(id: Long): RpgElement?

}

fun RpgElementCenter.getElement(id: Long): RpgElement = getElementOrNull(id)!!

/**
 * 可变的[RpgElementCenter]
 * @author yumetsuki
 */
interface MutableRpgElementCenter : RpgElementCenter {

    fun registerElement(id: Long, element: RpgElement)

}

/**
 * 游戏元素，它是一个被预定义的游戏中对象类型，例如，游戏中存在「HP 药水」这种道具
 * 在程序中，它是一个元素（类别），一个角色有很多个「HP 药水」，则这些为游戏中实际存在的对象[RpgObject]
 */
sealed interface RpgElement {

    val id: Long

    fun createRpgObject(): RpgObject

}

const val SYSTEM_ELEMENT_ID = Long.MIN_VALUE

/**
 * [RpgElement] 数组实现，会创建子[RpgElement]对应的[RpgObject]
 * @author yumetsuki
 */
class RpgElementArray(
    private val content: List<RpgElement>
): RpgElement, List<RpgElement> by content {

    override val id: Long = SYSTEM_ELEMENT_ID

    override fun createRpgObject(): RpgObject = RpgObjectArray(
        content.map(RpgElement::createRpgObject)
    )

}

class CommonRpgElementCenter : MutableRpgElementCenter {

    private val builders = mutableMapOf<Long, RpgElement>()

    override fun registerElement(id: Long, element: RpgElement) {
        builders[id] = element
    }

    override fun getElementOrNull(id: Long): RpgElement? {
        return builders[id]
    }

}