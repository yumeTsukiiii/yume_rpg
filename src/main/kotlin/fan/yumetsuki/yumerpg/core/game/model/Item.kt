package fan.yumetsuki.yumerpg.core.game.model

import fan.yumetsuki.yumerpg.core.model.*

/**
 * 创建一个消耗品，一个消耗品是具备[ConsumableAbility]和[CountAbility]能力的[RpgModel],
 * @param name 消耗品名称
 * @param abilities 消耗品具备的能力
 * @param count 消耗品数量
 * @author yumetsuki
 */
fun consumable(name: String, abilities: List<RpgAbility<*, *, *, *>>, description: String = "", count: Int = 0) : RpgModel {

    return CommonRpgModel(
        meta = mapMeta(
            "name" to name,
            "description" to description
        ),
        abilities = listOf(
            ConsumableAbility(),
            CountAbility(value = count),
            *abilities.filter {
                it !is ConsumableAbility && it !is CountAbility
            }.toTypedArray()
        )
    )

}

/**
 * 判断该对象是否为消耗品
 * @return 若为消耗品，返回 true 否则为 false
 * @author yumetsuki
 */
fun RpgModel.isConsumable(): Boolean = getAbility<ConsumableAbility, _, _, _, _>() != null

/**
 * 获取一个对象的个数
 * @return 若该对象为一个物品，则 count 返回 >= 0 的值，否则返回 null
 * @author yumetsuki
 */
fun RpgModel.count(): Int? = getAbility<CountAbility, _, _, _, _>()?.value

/**
 * 个数能力，只要是物品都可以拥有个数，例如消耗品 / 状态
 * @author yumetsuki
 */
class CountAbility(
    override val name: String = "count",
    override val alias: String? = "个数",
    override var value: Int
): PropertyAbility<Int, RpgModel>

/**
 * 消耗品特有的能力，会将除该能力以外的全部[NoParamCommandAbility]能力执行
 * 且消耗品拥有个数，个数消耗完则不会再执行
 * @author yumetsuki
 */
class ConsumableAbility(
    override val name: String = "count",
    override val alias: String? = "消耗品"
) : NoParamCommandAbility<RpgModel, RpgModel> {

    override suspend fun execute(owner: RpgModel, target: RpgModel) {
        owner.getAbility<CountAbility, _, _, _, _>()?.apply {
            // 消耗品空了，不能继续消耗
            if (value <= 0) {
                return
            }
            value--
            // 这里的 owner 就是消耗品对象，仅能执行那些不需要参数并且直接执行的能力
            owner.abilities().filterIsInstance<NoParamCommandAbility<RpgModel, RpgModel>>()
                .forEach {
                    it.execute(owner, target)
                }
        }
    }

}

/**
 * 改变对象生命值的能力，通常用于恢复技能/药水
 * @author yumetsuki
 */
class HpChange(
    /**
     * 计算生命值变化的表达式，返回值为生命值的增减值
     */
    private val expr: suspend (owner: RpgModel, target: RpgModel) -> Int,
    override val name: String = "HpChange",
    override val alias: String? = "生命值改变，用于增减生命值"
) : NoParamCommandAbility<RpgModel, RpgModel> {

    override suspend fun execute(owner: RpgModel, target: RpgModel) {
        target.getAbility<HpAbility, _, _, _, _>()?.apply {
            value += expr(owner, target)
        }
    }

}