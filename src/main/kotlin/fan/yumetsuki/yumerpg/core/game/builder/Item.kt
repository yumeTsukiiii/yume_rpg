package fan.yumetsuki.yumerpg.core.game.builder

import fan.yumetsuki.yumerpg.core.game.gameEngine
import fan.yumetsuki.yumerpg.core.game.model.HpChange
import fan.yumetsuki.yumerpg.core.model.RpgAbility
import fan.yumetsuki.yumerpg.core.model.RpgModel

/**
 * 消耗品构建器，用于构建消耗品对象
 * @see fan.yumetsuki.yumerpg.core.game.model.consumable
 * @author yumetsuki
 */
object ConsumableBuilder : RpgModelBuilder {

    override val id: Long = 1L

    override fun build(param: Map<String, Any>): RpgModel {
        TODO("Not yet implemented")
    }

}

/**
 * 生命值改变能力构建器
 * @see fan.yumetsuki.yumerpg.core.game.model.HpChange
 * @author yumetsuki
 */
object HpChangeBuilder : RpgAbilityBuilder {

    override val id: Long = 2L

    // TODO 封装 param，不要直接透出 map
    override fun build(param: Map<String, Any>): RpgAbility<*, *, *, *> {
        val expr = param["expr"]?.toString() ?: error("Hp 回复必须填写计算表达式")
        return HpChange({ owner, target ->
            expr.toIntOrNull() ?: gameEngine.execScript(mapOf(
                "owner" to owner,
                "target" to target
            ), expr)
        })
    }

}