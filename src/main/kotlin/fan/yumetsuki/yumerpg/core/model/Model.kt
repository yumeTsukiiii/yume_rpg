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
    fun abilities(): List<RpgAbility<*, *, *, *>>

    fun <Ability: RpgAbility<Owner, Target, Param, Result>, Owner, Target, Param, Result> getAbility(abilityClass: Class<Ability>): Ability?
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
