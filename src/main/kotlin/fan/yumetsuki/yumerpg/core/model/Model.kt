package fan.yumetsuki.yumerpg.core.model

import fan.yumetsuki.yumerpg.core.script.ScriptSerializable
import fan.yumetsuki.yumerpg.core.utils.RangeProperty
import fan.yumetsuki.yumerpg.core.utils.putSerializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject

/**
 * 游戏模型，可以是游戏中的各种物品、人物、地图块等
 * @author yumetsuki
 */
interface RpgModel : ScriptSerializable {
    /**
     * 游戏模型的元信息，通常用来存储一些不常在游戏中被改变的数据，例如人物名，描述等等
     * 若需要存储游戏对象的属性，请使用[PropertyAbility]
     */
    fun meta(): RpgMeta
    /**
     * 游戏模型所具备的能力，例如，某种道具可被使用，也可被装备
     */
    fun abilities(): List<RpgAbility<*, *, *, *>>

    fun <Ability: RpgAbility<*, *, *, *>> getAbility(abilityClass: Class<Ability>): Ability?

    override fun toScriptObj(): JsonElement = buildJsonObject {

        // 将所有属性持有能力的原始值塞进对象
        abilities().filterIsInstance<PropertyAbility<*, RpgModel>>().forEach {
            putSerializable(it.name.lowercase(), it.value)
        }

        // 将所有原始值类型的 meta 信息塞进对象
        meta().all().forEach { (k, v) ->
            putSerializable(k, v)
        }

    }
}

/**
 * 常规的游戏对象，拥有元数据描述，具备一定逻辑执行能力
 * @author yumetsuki
 */
class CommonRpgModel(
    private val meta: RpgMeta,
    private val abilities: List<RpgAbility<*, *, *, *>>
) : RpgModel {
    override fun meta(): RpgMeta = meta

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