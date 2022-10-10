package fan.yumetsuki.yumerpg.core.game.builder

import fan.yumetsuki.yumerpg.core.model.RpgAbility
import fan.yumetsuki.yumerpg.core.model.RpgModel
import fan.yumetsuki.yumerpg.core.protocol.RpgAbilityBuilder
import fan.yumetsuki.yumerpg.core.protocol.RpgBuildObject
import fan.yumetsuki.yumerpg.core.protocol.RpgModelBuilder

/**
 * 消耗品构建器，用于构建消耗品对象
 * @see fan.yumetsuki.yumerpg.core.game.model.consumable
 * @author yumetsuki
 */
object ConsumableBuilder : RpgModelBuilder {

    override val id: Long = 1L

    override fun build(buildObject: RpgBuildObject?): RpgModel {
        TODO("Not yet implemented")
    }

}

/**
 * 属性值改变能力构建器
 * @see fan.yumetsuki.yumerpg.core.game.model.PropertyChangeAbility
 * @author yumetsuki
 */
object PropertyChangeAbilityBuilder : RpgAbilityBuilder {

    override val id: Long = 2L

    override fun build(buildObject: RpgBuildObject?): RpgAbility<*, *, *, *> {
        TODO("Not yet implemented")
    }

}