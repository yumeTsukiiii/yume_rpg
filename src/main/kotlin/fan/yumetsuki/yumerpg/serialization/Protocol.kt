package fan.yumetsuki.yumerpg.serialization

/**
 * Rpg 对象协议，用来实现游戏中定义的对象的反序列化
 * @param Data 序列化对象所持有的数据类型
 */
interface RpgElementProtocol<Content, Data> {

    fun decodeFromContent(content: Content) : RpgElement<Data>

}

/**
 * Rpg 元素协议，用来实现游戏中实际存在对象的序列化和反序列化的规则
 * @param Data 序列化对象所持有的数据类型
 * @author yumetsuki
 */
interface RpgObjectProtocol<Content, Data> {

    fun encodeToContent(rpgObjSerializeContext: RpgObjSerializeContext<Data>, serializable: RpgObject): Content

    fun decodeFromContent(rpgObjSerializeContext: RpgObjSerializeContext<Data>, content: Content): RpgObject

}

/**
 * [RpgObject] 序列化上下文，提供序列化/反序列化的一些参数和可选策略等
 * @param Data 序列化对象所持有的数据类型
 */
interface RpgObjSerializeContext<Data> {

    fun getRpgElementOrNull(id: Long): RpgElement<Data>?

    fun getRpgObjConstructorOrNull(id: Long): RpgObjectConstructor?

}

fun <Data> RpgObjSerializeContext<Data>.getRpgObjConstructorOrNullByElementId(elementId: Long): RpgObjectConstructor? = getRpgElementOrNull(
    elementId
)?.constructorId?.let {
    getRpgObjConstructorOrNull(it)
}


fun <Data> RpgObjSerializeContext<Data>.getRpgElement(id: Long): RpgElement<Data> = getRpgElementOrNull(id)!!

fun <Data> RpgObjSerializeContext<Data>.getRpgObjConstructor(id: Long): RpgObjectConstructor = getRpgObjConstructorOrNull(id)!!

class CommonRpgObjSerializeContext<Data>(
    private val rpgElementCenter: RpgElementCenter<Data>,
    private val rpgObjConstructorCenter: RpgObjConstructorCenter
) : RpgObjSerializeContext<Data> {

    override fun getRpgElementOrNull(id: Long): RpgElement<Data>? {
        return rpgElementCenter.getElementOrNull(id)
    }

    override fun getRpgObjConstructorOrNull(id: Long): RpgObjectConstructor? {
        return rpgObjConstructorCenter.getConstructorOrNull(id)
    }

}