package fan.yumetsuki.yumerpg.core.serialization

/**
 * [RpgElement] 中心，集中管理所有注册在系统中的 RpgElement
 * 由系统解析数据，生成[RpgElement]注册在该 Center 中[MutableRpgElementCenter]
 * @author yumetsuki
 */
interface RpgElementCenter<Data> {

    fun getElementOrNull(id: Long): RpgElement<Data>?

}

fun <Data> RpgElementCenter<Data>.getElement(id: Long): RpgElement<Data> = getElementOrNull(id)!!

/**
 * 可变的[RpgElementCenter]
 * @author yumetsuki
 */
interface MutableRpgElementCenter<Data> : RpgElementCenter<Data> {

    fun registerElement(element: RpgElement<Data>)

}

/**
 * 游戏元素，它是一个被预定义的游戏中对象类型，例如，游戏中存在「HP 药水」这种道具
 * 在程序中，它是一个元素（类别），一个角色有很多个「HP 药水」，则这些为游戏中实际存在的对象[RpgObject]
 */
interface RpgElement<Data> {

    val id: Long

    val constructorId: Long

    val data: Data?

    fun createRpgObject(rpgElementContext: RpgElementContext<Data>): RpgObject

}

interface RpgElementContext<Data> {

    val current: RpgElement<Data>

    val data: Data?

    fun getRpgElementOrNull(id: Long): RpgElement<Data>?

    fun getRpgObjectConstructorOrNull(id: Long): RpgObjectConstructor?

}

/**
 * 替换必要数据，其他通用方法代理实现 [RpgElementContext] 的 Context
 * 用于递归创建 RpgObject 的场景
 * @author yumetsuki
 */
class DelegateRpgElementContext<Data>(
    override val current: RpgElement<Data>,
    delegate: RpgElementContext<Data>,
    override val data: Data? = null
) : RpgElementContext<Data> by delegate

fun <Data> RpgElementContext<Data>.getRpgElement(id: Long): RpgElement<Data> = getRpgElementOrNull(id)!!

fun <Data> RpgElementContext<Data>.getConstructor(id: Long): RpgObjectConstructor = getRpgObjectConstructorOrNull(id)!!

const val UNKNOWN_ELEMENT_ID = Long.MIN_VALUE

/**
 * [RpgElement] 数组实现，会创建子[RpgElement]对应的[RpgObject]
 * @author yumetsuki
 */
class RpgElementArray<Data>(
    private val content: List<RpgElement<Data>>
): RpgElement<Data>, List<RpgElement<Data>> by content {

    override val id: Long = UNKNOWN_ELEMENT_ID

    override val constructorId: Long = UNKNOWN_CONSTRUCTOR_ID

    override fun createRpgObject(rpgElementContext: RpgElementContext<Data>): RpgObject {
        return RpgObjectArray(content.map { it.createRpgObject(rpgElementContext) })
    }

    override val data: Data? = null

}

class CommonRpgElementCenter<Data>: MutableRpgElementCenter<Data> {

    private val builders = mutableMapOf<Long, RpgElement<Data>>()

    override fun registerElement(element: RpgElement<Data>) {
        builders[element.id] = element
    }

    override fun getElementOrNull(id: Long): RpgElement<Data>? {
        return builders[id]
    }

}