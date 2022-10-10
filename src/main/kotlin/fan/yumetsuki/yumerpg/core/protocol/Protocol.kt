package fan.yumetsuki.yumerpg.core.protocol

import fan.yumetsuki.yumerpg.core.model.RpgAbility
import fan.yumetsuki.yumerpg.core.model.RpgModel
import kotlinx.serialization.json.*
import kotlin.reflect.KClass

/**
 * Rpg 构建协议，协议描述了游戏中所有对象的定义
 * 该协议将协议文本转化为 Native Object
 * @author yumetsuki
 */
interface RpgBuildProtocol {

    /**
     * 注册协议构建所需要的[RpgBuilder]，[RpgBuilder]将协议中的描述转化为[RpgModel]
     * @param builder [RpgBuilder]
     * @param buildResultClass 构建器返回值的类别，用来对构建器分类，防止[RpgBuilder.id]冲突
     */
    fun <T: Any> registerBuilder(builder: RpgBuilder<T>, buildResultClass: KClass<T>)

    /**
     * 解析文本协议为[RpgModel]列表
     * @return [RpgModel]列表
     */
    suspend fun load(text: String): List<RpgModel>

}

inline fun <reified T: Any> RpgBuildProtocol.registerBuilder(builder: RpgBuilder<T>) {
    registerBuilder(builder, T::class)
}

/**
 * Rpg 构建协议的 JSON 协议实现
 * @author yumetsuki
 */
class RpgBuildJsonProtocol : RpgBuildProtocol {

    /**
     * 注册的 builder 记录，用 class 和 id 作为 唯一标识
     */
    private val builders: MutableMap<Pair<KClass<*>, Long>, RpgBuilder<*>> = mutableMapOf()

    override fun <T : Any> registerBuilder(builder: RpgBuilder<T>, buildResultClass: KClass<T>) {
        builders[buildResultClass to builder.id] = builder
    }

    override suspend fun load(text: String): List<RpgModel> {
        return when(val jsonProtocol = Json.parseToJsonElement(text)) {
            is JsonObject -> listOf(loadRpgModel(jsonProtocol))
            is JsonArray -> jsonProtocol.filterIsInstance<JsonObject>().map(this::loadRpgModel)
            else -> error("Json 文本必须为一个 JsonArray 或者 JsonObject")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadRpgModel(jsonProtocol: JsonObject): RpgModel {
        // TODO 重复代码优化
        val builderId = jsonProtocol["builder"]?.jsonPrimitive?.long ?: error("RpgModel 构建必须指定一个类型为 Long 的 builderId")
        val param = jsonProtocol["param"]?.let { it as? JsonObject ?: error("RpgModel param 必须为一个 JsonObject") }
        // TODO 找一个更好的方式存储 builder，而不是使用强转
        return (builders[RpgModel::class to builderId] as? RpgModelBuilder)?.build(
            param?.let {
                RpgBuildJsonObject(it)
            }
        ) ?: error("创建 RpgAbility 失败")
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadRpgAbility(jsonProtocol: JsonObject): RpgAbility<*, *, *, *> {
        val builderId = jsonProtocol["builder"]?.jsonPrimitive?.long ?: error("RpgAbility 构建必须指定一个类型为 Long 的 builderId")
        val param = jsonProtocol["param"]?.let { it as? JsonObject ?: error("RpgAbility param 必须为一个 JsonObject") }
        return (builders[RpgAbility::class to builderId] as? RpgAbilityBuilder)?.build(
            param?.let {
                RpgBuildJsonObject(it)
            }
        ) ?: error("创建 RpgAbility 失败")
    }

    /**
     * 构建[RpgModel]时的 Json 协议参数
     * @author yumetsuki
     */
    inner class RpgBuildJsonObject(
        private val protocol: JsonObject
    ): RpgBuildObject {

        private lateinit var cacheAbilities: List<RpgAbility<*, *, *, *>>

        override fun getInt(key: String): Int = protocol[key]?.jsonPrimitive?.int ?: error("$key 对象为空或者不为 int 类型")

        override fun getDouble(key: String): Double = protocol[key]?.jsonPrimitive?.double ?: error("$key 对象为空或者不为 double 类型")

        override fun getString(key: String): String = protocol[key]?.jsonPrimitive?.content ?: error("$key 对象为空或者不为 string 类型")

        // TODO 考虑重构，因为针对 RpgModel 的构建，它的协议里是需要 meta 和 abilities 解析的，但对于 RpgAbility 则不需要，全部这样则限制了 RpgAbility 的 key
        override fun getAbilities(): List<RpgAbility<*, *, *, *>> {
            if (!this::cacheAbilities.isInitialized) {
                cacheAbilities = protocol["abilities"]?.let {
                    it as? JsonArray ?: error("abilities 必须为一个 JsonArray")
                }?.filterIsInstance<JsonObject>()?.map {
                    loadRpgAbility(it)
                } ?: listOf()
            }
            return cacheAbilities
        }

    }

}