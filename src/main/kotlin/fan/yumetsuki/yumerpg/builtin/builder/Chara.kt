package fan.yumetsuki.yumerpg.builtin.builder

import fan.yumetsuki.yumerpg.core.serialization.*
import fan.yumetsuki.yumerpg.serialization.RpgObject
import fan.yumetsuki.yumerpg.serialization.RpgObjectConstructContext
import fan.yumetsuki.yumerpg.serialization.RpgObjectConstructor
import fan.yumetsuki.yumerpg.serialization.RpgObjectDeconstructContext

/**
 * 属性能力构建器，通常用于给某个角色添加可变的属性值，例如可变的角色名称
 * @author yumetsuki
 * {}
 */
class PropertyAbilityJsonConstructor(override val id: Long) : RpgObjectConstructor {

    override fun construct(context: RpgObjectConstructContext): RpgObject {
        TODO("Not yet implemented")
    }

    override fun deconstruct(context: RpgObjectDeconstructContext) {
        TODO("Not yet implemented")
    }

    companion object {

        const val NAME = "name"
        const val TYPE = "type"
        const val ALIAS = "alias"
        const val VALUE = "value"

    }

}