package fan.yumetsuki.yumerpg.builtin.constructor

import fan.yumetsuki.yumerpg.builtin.PropertyAbility
import fan.yumetsuki.yumerpg.builtin.rpgobject.BooleanPropertyAbility
import fan.yumetsuki.yumerpg.builtin.rpgobject.NumberPropertyAbility
import fan.yumetsuki.yumerpg.builtin.rpgobject.StringPropertyAbility
import fan.yumetsuki.yumerpg.serialization.*

/**
 * 属性能力构建器，通常用于给某个角色添加可变的属性值，例如可变的角色名称
 * @author yumetsuki
 */
class PropertyAbilityConstructor : RpgObjectConstructor {

    override val id: Long = ID

    override fun construct(context: RpgObjectConstructContext): RpgObject {
        val type = context.getString(TYPE)
        val name = context.getString(NAME)
        val alias = context.getStringOrNull(ALIAS)
        return when(type) {
            "int" -> NumberPropertyAbility(context.getIntOrNull(VALUE) ?: 0, name, alias, context.elementId)
            "double" -> NumberPropertyAbility(context.getDoubleOrNull(VALUE) ?: 0, name, alias, context.elementId)
            "string" -> StringPropertyAbility(context.getStringOrNull(VALUE) ?: "", name, alias, context.elementId)
            "boolean" -> BooleanPropertyAbility(context.getBooleanOrNull(VALUE) ?: false, name, alias, context.elementId)
            else -> error("不支持的属性类型 $type")
        }
    }

    override fun deconstruct(context: RpgObjectDeconstructContext) {
        context.deconstruct {
            context.rpgObject<PropertyAbility<Any, *>>().value.let {
                put(VALUE, it)
            }
        }
    }

    companion object {

        const val ID = 1L

        const val NAME = "name"
        const val TYPE = "type"
        const val ALIAS = "alias"
        const val VALUE = "value"

    }

}