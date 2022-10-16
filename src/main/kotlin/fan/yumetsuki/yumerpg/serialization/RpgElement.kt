package fan.yumetsuki.yumerpg.serialization

/**
 * [RpgElement] 中心，集中管理所有注册在系统中的 RpgElement
 * 由系统解析数据，生成[RpgElement]注册在该 Center 中[MutableRpgElementCenter]
 * @author yumetsuki
 */
interface RpgElementCenter {

    fun getElementOrNull(id: Long): RpgElement?

}

fun <Data> RpgElementCenter.getElement(id: Long): RpgElement = getElementOrNull(id)!!

/**
 * 可变的[RpgElementCenter]
 * @author yumetsuki
 */
interface MutableRpgElementCenter : RpgElementCenter {

    fun registerElement(element: RpgElement)

}

/**
 * 游戏元素，它是一个被预定义的游戏中对象类型，例如，游戏中存在「HP 药水」这种道具
 * 在程序中，它是一个元素（类别），一个角色有很多个「HP 药水」，则这些为游戏中实际存在的对象[RpgObject]
 */
interface RpgElement : RpgDataHolder {

    val id: Long

    val constructorId: Long

    fun createRpgObject(rpgElementContext: RpgElementContext): RpgObject

}

interface RpgElementContext: RpgDataHolder {

    val current: RpgElement

    fun getRpgElementOrNull(id: Long): RpgElement?

    fun getRpgObjectConstructorOrNull(id: Long): RpgObjectConstructor?

}

fun RpgElementContext.getRpgElement(id: Long): RpgElement = getRpgElementOrNull(id)!!

fun RpgElementContext.getConstructor(id: Long): RpgObjectConstructor = getRpgObjectConstructorOrNull(id)!!

const val UNKNOWN_ELEMENT_ID = Long.MIN_VALUE

/**
 * [RpgElement] 数组实现，会创建子[RpgElement]对应的[RpgObject]
 * @author yumetsuki
 */
class RpgElementArray(
    private val content: List<RpgElement>
): RpgElement, List<RpgElement> by content {

    override val id: Long = UNKNOWN_ELEMENT_ID

    override val constructorId: Long = UNKNOWN_CONSTRUCTOR_ID

    override fun createRpgObject(rpgElementContext: RpgElementContext): RpgObject {
        return RpgObjectArray(content.map { it.createRpgObject(rpgElementContext) })
    }

}

class CommonRpgElementCenter: MutableRpgElementCenter {

    private val builders = mutableMapOf<Long, RpgElement>()

    override fun registerElement(element: RpgElement) {
        builders[element.id] = element
    }

    override fun getElementOrNull(id: Long): RpgElement? {
        return builders[id]
    }

}