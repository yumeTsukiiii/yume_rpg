package fan.yumetsuki.yumerpg.serialization

/**
 * Rpg 对象协议，用来实现游戏中定义的对象的反序列化
 */
interface RpgElementProtocol<Content> {

    fun decodeFromContent(content: Content) : RpgElement

}

/**
 * Rpg 元素协议，用来实现游戏中实际存在对象的序列化和反序列化的规则
 * @author yumetsuki
 */
interface RpgObjectProtocol<Content> {

    suspend fun encodeToContent(rpgObjSerializeContext: RpgObjSerializeContext, serializable: RpgObject): Content

    suspend fun decodeFromContent(rpgObjSerializeContext: RpgObjSerializeContext, content: Content): RpgObject

}

/**
 * [RpgObject] 序列化上下文，提供序列化/反序列化的一些参数和可选策略等
 */
interface RpgObjSerializeContext {

    fun getRpgElementOrNull(id: Long): RpgElement?

    fun getRpgObjConstructorOrNull(id: Long): RpgObjectConstructor?

}

fun RpgObjSerializeContext.getRpgObjConstructorOrNullByElementId(elementId: Long): RpgObjectConstructor? = getRpgElementOrNull(
    elementId
)?.constructorId?.let {
    getRpgObjConstructorOrNull(it)
}


fun RpgObjSerializeContext.getRpgElement(id: Long): RpgElement = getRpgElementOrNull(id)!!

fun RpgObjSerializeContext.getRpgObjConstructor(id: Long): RpgObjectConstructor = getRpgObjConstructorOrNull(id)!!

class CommonRpgObjSerializeContext(
    private val rpgElementCenter: RpgElementCenter,
    private val rpgObjConstructorCenter: RpgObjConstructorCenter
) : RpgObjSerializeContext {

    override fun getRpgElementOrNull(id: Long): RpgElement? {
        return rpgElementCenter.getElementOrNull(id)
    }

    override fun getRpgObjConstructorOrNull(id: Long): RpgObjectConstructor? {
        return rpgObjConstructorCenter.getConstructorOrNull(id)
    }

}