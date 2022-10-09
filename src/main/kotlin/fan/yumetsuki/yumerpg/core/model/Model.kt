package fan.yumetsuki.yumerpg.core.model

import fan.yumetsuki.yumerpg.core.script.ScriptSerializable
import fan.yumetsuki.yumerpg.core.utils.putSerializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject

/**
 * 游戏模型，可以是游戏中的各种物品、人物、地图块等
 * @author yumetsuki
 */
interface RpgModel : ScriptSerializable {
    /**
     * 游戏模型的元信息，例如人物、道具包含名称等等
     */
    fun meta(): RpgMeta
    /**
     * 游戏模型所具备的能力，例如，某种道具可被使用，也可被装备
     */
    fun abilities(): List<RpgAbility<*, *, *, *>>

    fun <Ability: RpgAbility<Owner, Target, Param, Result>, Owner, Target, Param, Result> getAbility(abilityClass: Class<Ability>): Ability?

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

    override fun <Ability : RpgAbility<Owner, Target, Param, Result>, Owner, Target, Param, Result> getAbility(
        abilityClass: Class<Ability>
    ): Ability? = abilities.filterIsInstance(abilityClass).firstOrNull()

}

inline fun <reified Ability: RpgAbility<Owner, Target, Param, Result>, Owner, Target, Param, Result> RpgModel.getAbility(): Ability? {
    return getAbility(Ability::class.java)
}
