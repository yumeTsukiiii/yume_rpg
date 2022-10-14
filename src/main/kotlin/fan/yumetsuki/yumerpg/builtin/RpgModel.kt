package fan.yumetsuki.yumerpg.builtin

import fan.yumetsuki.yumerpg.serialization.RpgObject

/**
 * 游戏模型，可以是游戏中的各种物品、人物、地图块等
 * @author yumetsuki
 */
interface RpgModel : RpgObject {

    /**
     * 游戏模型的元信息，通常用来存储一些不常在游戏中被改变的数据，例如人物名，描述等等
     * 若需要存储游戏对象的属性，请使用[PropertyAbility]
     */
    fun meta(): RpgData

    /**
     * 游戏模型所具备的能力，例如，某种道具可被使用，也可被装备
     */
    fun abilities(): List<RpgAbility<*, *, *, *>>

    fun <Ability: RpgAbility<*, *, *, *>> getAbility(abilityClass: Class<Ability>): Ability?

}

/**
 * 常规的游戏对象，拥有元数据描述，具备一定逻辑执行能力
 * @author yumetsuki
 */
class CommonRpgModel(
    override val elementId: Long,
    private val meta: RpgData,
    private val abilities: List<RpgAbility<*, *, *, *>>
) : RpgModel {
    override fun meta(): RpgData = meta

    override fun abilities(): List<RpgAbility<*, *, *, *>> = abilities

    override fun <Ability : RpgAbility<*, *, *, *>> getAbility(
        abilityClass: Class<Ability>
    ): Ability? = abilities.filterIsInstance(abilityClass).firstOrNull()

}

inline fun <reified Ability: RpgAbility<*, *, *, *>> RpgModel.getAbility(): Ability? {
    return getAbility(Ability::class.java)
}

suspend inline fun <reified Ability: RpgAbility<RpgModel, Target, Param, Result>, Target, Param, Result> RpgModel.execAbility(
    target: Target, param: Param
): Result? {
    return getAbility<Ability>()?.execute(this, target, param)
}

fun <T> RpgModel.getProperty(name: String): T {
    return abilities().filterIsInstance<PropertyAbility<T, RpgModel>>().find {
        it.name == name
    }?.value ?: error("未能找到名为 $name 的属性")
}

fun <T> RpgModel.getPropertyAbilityOrNull(name: String): PropertyAbility<T, RpgModel>? {
    return abilities().filterIsInstance<PropertyAbility<T, RpgModel>>().find {
        it.name == name
    }
}

fun <T: Comparable<T>> RpgModel.getRangePropertyAbilityOrNull(name: String): RangePropertyAbility<T, RpgModel>? {
    return abilities().filterIsInstance<RangePropertyAbility<T, RpgModel>>().find {
        it.name == name
    }
}

inline fun <T> RpgModel.changeProperty(name: String, onChange: (value: T) -> T) {
    getPropertyAbilityOrNull<T>(name)?.let {
        it.value = onChange(it.value)
    }
}

inline fun <T: Comparable<T>> RpgModel.changeRangePropertyMax(name: String, onChange: (value: T) -> T) {
    getRangePropertyAbilityOrNull<T>(name)?.let {
        it.maxValue = onChange(it.maxValue)
    }
}

inline fun <T: Comparable<T>> RpgModel.changeRangePropertyMin(name: String, onChange: (value: T) -> T) {
    getRangePropertyAbilityOrNull<T>(name)?.let {
        it.minValue = onChange(it.minValue)
    }
}

/**
 * 游戏元信息，通过 kv 的方式存储，例如游戏人物会存储 name 信息
 * @author yumetsuki
 */
interface RpgData {

    /**
     * 获取元信息
     * @param key 元信息的存储 key
     * @return 返回 key 对应的元数据
     */
    fun <T> getOrNull(key: String): T?

    fun <T> get(key: String) = getOrNull<T>(key)!!

    /**
     * 获取所有元信息
     * @return 元信息的 kv 键值对
     */
    fun all(): Map<String, Any>

    /**
     * 获取所有元信息的 key
     * @return 所有元信息的[String]key
     */
    fun allKey(): List<String>

    companion object {
        val Empty = object : RpgData {
            override fun <T> getOrNull(key: String): T? = null
            override fun all(): Map<String, Any> = emptyMap()
            override fun allKey(): List<String> = emptyList()
        }
    }
}

/**
 * 可变的[RpgData]，通常用来存储一些状态值
 * @author yumetsuki
 */
interface MutableRpgData: RpgData {

    /**
     * 存储元信息
     * @param key 元信息的存储 key
     * @param value 存储在 key 位置对应的元数据
     */
    fun set(key: String, value: Any)

}

@Suppress("UNCHECKED_CAST")
class MapMutableRpgData(
    vararg data: Pair<String, Any>
): MutableRpgData {

    private val map: MutableMap<String, Any> = mutableMapOf()

    init {
        data.forEach { (key, value) ->
            map[key] = value
        }
    }

    override fun <T> getOrNull(key: String): T? = map[key] as T

    override fun set(key: String, value: Any) {
        map[key] = value
    }

    override fun all(): Map<String, Any> = map

    override fun allKey(): List<String> = map.keys.toList()
}

fun mapRpgData(vararg data: Pair<String, Any>): MutableRpgData = MapMutableRpgData(*data)

fun mapRpgMeta(vararg data: Pair<String, Any>): RpgData = mapRpgData(*data)