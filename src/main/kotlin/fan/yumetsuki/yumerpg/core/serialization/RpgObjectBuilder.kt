package fan.yumetsuki.yumerpg.core.serialization

/**
 * [RpgObjectBuilder] 中心，集中管理所有注册在系统中的 Builder
 * 系统内置一部分实现，允许用户外部注入，见[MutableRpgObjBuilderCenter]
 * @author yumetsuki
 */
interface RpgObjBuilderCenter {

    fun getRpgObjectBuilderOrNull(id: Long): RpgObjectBuilder?

}

/**
 * 可变的[RpgObjBuilderCenter]
 * @author yumetsuki
 */
interface MutableRpgObjBuilderCenter : RpgObjBuilderCenter {

    fun registerBuilder(id: Long, builder: RpgObjectBuilder)

}

fun RpgObjBuilderCenter.getRpgObjectBuilder(id: Long): RpgObjectBuilder = getRpgObjectBuilderOrNull(id)!!

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
    fun build(buildObject: RpgObjectBuildContext): RpgObject

}

/**
 * 构建器构建时上下文，它封装了单个原始数据对象的协议，用来获取构建时的各种信息
 * @author yumetsuki
 */
interface RpgObjectBuildContext {

    fun getIntOrNull(key: String): Int? = null

    fun getStringOrNull(key: String): String? = null

    fun getDoubleOrNull(key: String): Double? = null

    fun getBooleanOrNull(key: String): Boolean? = null

    fun getRpgObjectOrNull(key: String): RpgObject? = null

    companion object Empty : RpgObjectBuildContext

}

class CommonRpgObjBuilderCenter : MutableRpgObjBuilderCenter {

    private val builders = mutableMapOf<Long, RpgObjectBuilder>()

    override fun registerBuilder(id: Long, builder: RpgObjectBuilder) {
        builders[id] = builder
    }

    override fun getRpgObjectBuilderOrNull(id: Long): RpgObjectBuilder? {
        return builders[id]
    }

}
