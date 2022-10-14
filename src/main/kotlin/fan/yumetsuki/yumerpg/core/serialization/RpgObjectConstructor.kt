package fan.yumetsuki.yumerpg.core.serialization

/**
 * [RpgObjectConstructor] 中心，集中管理所有注册在系统中的 Builder
 * 系统内置一部分实现，允许用户外部注入，见[MutableRpgObjConstructorCenter]
 * @author yumetsuki
 */
interface RpgObjConstructorCenter<Data> {

    fun getConstructorOrNull(id: Long): RpgObjectConstructor<Data>?

}

/**
 * 可变的[RpgObjConstructorCenter]
 * @author yumetsuki
 */
interface MutableRpgObjConstructorCenter<Data> : RpgObjConstructorCenter<Data> {

    fun registerConstructor(constructor: RpgObjectConstructor<Data>)

}

const val UNKNOWN_CONSTRUCTOR_ID = Long.MIN_VALUE

fun <Data> RpgObjConstructorCenter<Data>.getConstructor(id: Long): RpgObjectConstructor<Data> = getConstructorOrNull(id)!!

/**
 * 游戏对象构建器，用于解析参数列表构建指定的对象，也可以将对象序列化为[RpgObjectData]
 * 该构建器含有泛型参数[Data]，用于适配各种协议的序列化
 * 可能构建出[RpgObject]
 * @author yumetsuki
 */
interface RpgObjectConstructor<Data> {

    /**
     * 构建器的 id，用于构建系统全局管理
     */
    val id: Long

    /**
     * 构建一个 [RpgObject] 对象
     * @return [RpgObject] 可能是游戏中的任何对象
     */
    fun construct(context: RpgObjectConstructContext<Data>): RpgObject

    /**
     * 解构一个 [RpgObject] 对象，它将 RpgObject 序列化为 [RpgObjectData]
     */
    fun deconstruct(context: RpgObjectDeconstructContext<Data>): RpgObjectData<Data>

}

interface RpgObjectData<out Data> {

    val elementId: Long

    val data: Data?
        get() = null

}

class RpgObjectDataArray<Data>(
    content: List<RpgObjectData<Data>>,
): RpgObjectData<Data>, List<RpgObjectData<Data>> by content {

    override val elementId: Long = UNKNOWN_ELEMENT_ID

}

interface RpgObjectContext<Data> {

    val elementId: Long

    fun getConstructorByElementIdOrNull(elementId: Long): RpgObjectConstructor<Data>? = null

}

/**
 * 构建器构建时上下文，它封装了单个原始数据对象的协议，用来获取构建时的各种信息
 * @author yumetsuki
 */
interface RpgObjectConstructContext<Data> : RpgObjectContext<Data> {

    fun getIntOrNull(key: String): Int? = null

    fun getStringOrNull(key: String): String? = null

    fun getDoubleOrNull(key: String): Double? = null

    fun getBooleanOrNull(key: String): Boolean? = null

    fun getRpgObjectOrNull(key: String): RpgObject? = null

}

interface RpgObjectDeconstructContext<Data> : RpgObjectContext<Data> {

    val rpgObject: RpgObject

}

inline fun <reified T> RpgObjectDeconstructContext<*>.rpgObject(): T {
    return rpgObject as? T ?: error("解构对象必须是一个 ${T::class.simpleName}")
}

class DelegateRpgObjectDeconstructContext<Data>(
    override val elementId: Long,
    delegate: RpgObjectDeconstructContext<Data>
) : RpgObjectDeconstructContext<Data> by delegate

fun <Data> RpgObjectDeconstructContext<Data>.delegateWithElementId(
    elementId: Long
) : RpgObjectDeconstructContext<Data> = DelegateRpgObjectDeconstructContext(elementId, this)

/**
 * 通过 [RpgObjectContext] 解构 [RpgObject]，通常用于一个 [RpgModel] 序列化 [RpgModel.abilities] 时的场景
 * @author yumetsuki
 */
fun <Data> RpgObjectDeconstructContext<Data>.deconstructRpgObject(rpgObject: RpgObject): RpgObjectData<Data>? {
    return getConstructorByElementIdOrNull(rpgObject.elementId)?.deconstruct(
        delegateWithElementId(rpgObject.elementId),
    )
}

class CommonRpgObjConstructorCenter<Data>: MutableRpgObjConstructorCenter<Data> {

    private val constructors = mutableMapOf<Long, RpgObjectConstructor<Data>>()

    override fun registerConstructor(constructor: RpgObjectConstructor<Data>) {
        constructors[constructor.id] = constructor
    }

    override fun getConstructorOrNull(id: Long): RpgObjectConstructor<Data>? {
        return constructors[id]
    }

}