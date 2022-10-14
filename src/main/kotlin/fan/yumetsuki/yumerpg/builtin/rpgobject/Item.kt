package fan.yumetsuki.yumerpg.builtin.rpgobject

import fan.yumetsuki.yumerpg.serialization.NoParamCommandAbility
import fan.yumetsuki.yumerpg.serialization.PropertyAbility
import fan.yumetsuki.yumerpg.serialization.RpgModel
import fan.yumetsuki.yumerpg.serialization.getAbility

/**
 * 判断该对象是否为消耗品
 * @return 若为消耗品，返回 true 否则为 false
 * @author yumetsuki
 */
fun RpgModel.isConsumable(): Boolean = getAbility<ConsumableAbility>() != null

/**
 * 获取一个对象的个数
 * @return 若该对象为一个物品，则 count 返回 >= 0 的值，否则返回最大值，它可无限使用
 * @author yumetsuki
 */
fun RpgModel.count(): Int = getAbility<CountAbility>()?.value ?: Int.MAX_VALUE

/**
 * 个数能力，只要是物品都可以拥有个数，例如消耗品 / 状态
 * @author yumetsuki
 */
class CountAbility(
    override val name: String = "count",
    override val alias: String? = "个数",
    override var value: Int,
    override val elementId: Long
): PropertyAbility<Int, RpgModel>

/**
 * 消耗品特有的能力，会将除该能力以外的全部[NoParamCommandAbility]能力执行
 * 且消耗品拥有个数，个数消耗完则不会再执行
 * @author yumetsuki
 */
class ConsumableAbility(
    override val name: String = "count",
    override val alias: String? = "消耗品",
    override val elementId: Long
) : NoParamCommandAbility<RpgModel, RpgModel> {

    override suspend fun execute(owner: RpgModel, target: RpgModel) {
        // 消耗品若无数量限制，则无限制使用
        owner.getAbility<CountAbility>()?.apply {
            // 消耗品空了，不能继续消耗
            if (value <= 0) {
                return
            }
            value--
        }
        // 这里的 owner 就是消耗品对象，仅能执行那些不需要参数并且直接执行的能力
        owner.abilities().filterIsInstance<NoParamCommandAbility<RpgModel, RpgModel>>()
            .forEach {
                it.execute(owner, target)
            }
        mutableMapOf<String, String>()["1"] = ""
    }

}