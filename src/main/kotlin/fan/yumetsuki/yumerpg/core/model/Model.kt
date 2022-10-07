package fan.yumetsuki.yumerpg.core.model

/**
 * 游戏模型，可以是游戏中的各种物品、人物、地图块等
 * @author yumetsuki
 */
interface RpgModel {
    /**
     * 游戏模型的元信息，例如人物、道具包含名称等等
     */
    fun meta(): RpgMeta
    /**
     * 游戏模型所具备的能力，例如，某种道具可被使用，也可被装备
     */
    fun abilities(): List<RpgAbility<*>>

    fun <Target, Ability: RpgAbility<Target>> getAbility(abilityClass: Class<Ability>): Ability?
}

class BaseRpgModel(
    private val meta: RpgMeta,
    private val abilities: List<RpgAbility<*>>
) : RpgModel {
    override fun meta(): RpgMeta = meta

    override fun abilities(): List<RpgAbility<*>> = abilities

    override fun <Target, Ability : RpgAbility<Target>> getAbility(
        abilityClass: Class<Ability>
    ): Ability? = abilities.filterIsInstance(abilityClass).firstOrNull()

}

