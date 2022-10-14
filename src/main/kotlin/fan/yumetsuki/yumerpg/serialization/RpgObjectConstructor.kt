package fan.yumetsuki.yumerpg.serialization

/**
 * [RpgObjectConstructor] 中心，集中管理所有注册在系统中的 Builder
 * 系统内置一部分实现，允许用户外部注入，见[MutableRpgObjConstructorCenter]
 * @author yumetsuki
 */
interface RpgObjConstructorCenter {

    fun getConstructorOrNull(id: Long): RpgObjectConstructor?

}

/**
 * 可变的[RpgObjConstructorCenter]
 * @author yumetsuki
 */
interface MutableRpgObjConstructorCenter : RpgObjConstructorCenter {

    fun registerConstructor(constructor: RpgObjectConstructor)

}

const val UNKNOWN_CONSTRUCTOR_ID = Long.MIN_VALUE

fun RpgObjConstructorCenter.getConstructor(id: Long): RpgObjectConstructor = getConstructorOrNull(id)!!

/**
 * 游戏对象构建器，用于解析参数列表构建指定的对象，也可以通过 [RpgObjectDeconstructContext] 将其序列化，具体序列化的对象依赖协议实现
 * 可能构建出[RpgObject]
 * @author yumetsuki
 */
interface RpgObjectConstructor {

    /**
     * 构建器的 id，用于构建系统全局管理
     */
    val id: Long

    /**
     * 构建一个 [RpgObject] 对象
     * @return [RpgObject] 可能是游戏中的任何对象
     */
    fun construct(context: RpgObjectConstructContext): RpgObject

    /**
     * 解构一个 [RpgObject] 对象，[RpgObjectDeconstructContext.deconstruct] 会将其中 RpgObject 解构为协议存储的对象
     */
    fun deconstruct(context: RpgObjectDeconstructContext)
}


interface RpgObjectContext {

    val elementId: Long

    fun getConstructorByElementIdOrNull(elementId: Long): RpgObjectConstructor? = null

}

/**
 * 构建器构建时上下文，它封装了单个原始数据对象的协议，用来获取构建时的各种信息
 * @author yumetsuki
 */
interface RpgObjectConstructContext : RpgObjectContext {

    fun getIntOrNull(key: String): Int? = null

    fun getStringOrNull(key: String): String? = null

    fun getDoubleOrNull(key: String): Double? = null

    fun getBooleanOrNull(key: String): Boolean? = null

    fun getRpgObjectOrNull(key: String): RpgObject? = null

}

interface RpgObjectDeconstructContext : RpgObjectContext {

    val rpgObject: RpgObject

    fun deconstruct(deconstruction: RpgObjectDataBuilder.() -> Unit)

}

/**
 * [RpgObject] 存储数据构建器，用于 [RpgObjectConstructor.deconstruct] 时对 RpgObject 的解构
 * 具体协议应当实现该 Builder 供开发者调用，将 kv 的值转化为具体的数据存储
 * @author yumetsuki
 */
interface RpgObjectDataBuilder {

    fun put(key: String, value: Int)

    fun put(key: String, value: String)

    fun put(key: String, value: Boolean)

    fun put(key: String, value: Double)

    fun put(key: String, value: RpgObject)

}

inline fun <reified T> RpgObjectDeconstructContext.rpgObject(): T {
    return rpgObject as? T ?: error("解构对象必须是一个 ${T::class.simpleName}")
}

class CommonRpgObjConstructorCenter: MutableRpgObjConstructorCenter {

    private val constructors = mutableMapOf<Long, RpgObjectConstructor>()

    override fun registerConstructor(constructor: RpgObjectConstructor) {
        constructors[constructor.id] = constructor
    }

    override fun getConstructorOrNull(id: Long): RpgObjectConstructor? {
        return constructors[id]
    }

}