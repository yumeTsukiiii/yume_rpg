package fan.yumetsuki.yumerpg.core.builtin.builder

import fan.yumetsuki.yumerpg.core.serialization.RpgAbility
import fan.yumetsuki.yumerpg.core.serialization.RpgModel
import fan.yumetsuki.yumerpg.core.serialization.RpgAbilityBuilder
import fan.yumetsuki.yumerpg.core.serialization.RpgObjectBuildContext
import fan.yumetsuki.yumerpg.core.serialization.RpgModelBuilder

/**
 * 消耗品构建器，用于构建消耗品对象
 * @see fan.yumetsuki.yumerpg.core.game.model.consumable
 * @author yumetsuki
 */
object ConsumableObjectBuilder : RpgModelBuilder {

    override val id: Long = 1L

    override fun build(buildObject: RpgObjectBuildContext?): RpgModel {
        TODO("Not yet implemented")
    }

}

object PropertyAbilityObjectBuilder: RpgAbilityBuilder {
    override val id: Long
        get() = 2L

    override fun build(buildObject: RpgObjectBuildContext?): RpgAbility<*, *, *, *> {
        TODO("Not yet implemented")
    }

}

/**
 * 属性值改变能力构建器
 * @see fan.yumetsuki.yumerpg.core.game.model.PropertyChangeAbility
 * @author yumetsuki
 */
object PropertyChangeAbilityObjectBuilder : RpgAbilityBuilder {

    override val id: Long = 3L

    override fun build(buildObject: RpgObjectBuildContext?): RpgAbility<*, *, *, *> {
        TODO("Not yet implemented")
    }

}