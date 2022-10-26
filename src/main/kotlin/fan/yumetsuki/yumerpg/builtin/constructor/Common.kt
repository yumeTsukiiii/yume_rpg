package fan.yumetsuki.yumerpg.builtin.constructor

import fan.yumetsuki.yumerpg.builtin.*
import fan.yumetsuki.yumerpg.builtin.rpgobject.*
import fan.yumetsuki.yumerpg.serialization.*

/**
 * 属性能力构建器，通常用于给某个角色添加可变的属性值，例如可变的角色名称
 * @author yumetsuki
 */
class PropertyAbilityConstructor : RpgObjectConstructor {

    override fun construct(context: RpgObjectConstructContext): RpgObject {
        val type = context.getString(TYPE)
        val name = context.getString(NAME)
        val alias = context.getStringOrNull(ALIAS)
        return when(type) {
            "int" -> NumberPropertyAbility(context.getLongOrNull(VALUE) ?: 0, name, alias, context.elementId)
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

        const val NAME = "name"
        const val TYPE = "type"
        const val ALIAS = "alias"
        const val VALUE = "value"

    }

}

/**
 * 范围属性能力构建器，通常用于给某个角色添加可变的带上下限属性值，例如可变的 HP / MP
 * @author yumetsuki
 */
class RangePropertyAbilityConstructor : RpgObjectConstructor {

    override fun construct(context: RpgObjectConstructContext): RpgObject {
        val type = context.getString(TYPE)
        val name = context.getString(NAME)
        val alias = context.getStringOrNull(ALIAS)
        return when(type) {
            "long" -> {
                val maxValue = context.getLong(MAX_VALUE)
                val minValue = context.getLong(MIN_VALUE)
                val value = context.getLongOrNull(VALUE) ?: maxValue
                LongRangePropertyAbility(
                    RangeProperty(value, maxValue, minValue),
                    name, alias, context.elementId
                )
            }
            "double" -> {
                val maxValue = context.getDouble(MAX_VALUE)
                val minValue = context.getDouble(MIN_VALUE)
                val value = context.getDoubleOrNull(VALUE) ?: maxValue
                DoubleRangePropertyAbility(
                    RangeProperty(value, maxValue, minValue),
                    name, alias, context.elementId
                )
            }
            "string" -> {
                val maxValue = context.getString(MAX_VALUE)
                val minValue = context.getString(MIN_VALUE)
                val value = context.getStringOrNull(VALUE) ?: maxValue
                StringRangePropertyAbility(
                    RangeProperty(value, maxValue, minValue),
                    name, alias, context.elementId
                )
            }
            "boolean" -> {
                val maxValue = context.getBoolean(MAX_VALUE)
                val minValue = context.getBoolean(MIN_VALUE)
                val value = context.getBooleanOrNull(VALUE) ?: maxValue
                BooleanRangePropertyAbility(
                    RangeProperty(value, maxValue, minValue),
                    name, alias, context.elementId
                )
            }
            else -> error("不支持的属性类型 $type")
        }
    }

    override fun deconstruct(context: RpgObjectDeconstructContext) {
        context.deconstruct {
            context.rpgObject<RangePropertyAbility<*, *>>().value.let {
                put(VALUE, it)
                put(MAX_VALUE, it)
                put(MIN_VALUE, it)
            }
        }
    }

    companion object {

        const val NAME = "name"
        const val TYPE = "type"
        const val ALIAS = "alias"
        const val VALUE = "value"
        const val MAX_VALUE = "maxValue"
        const val MIN_VALUE = "minValue"

    }

}

/**
 * 属性改变能力构造器，通常用于一些消耗品的功能，比如 hp 回复
 * @author yumetsuki
 */
class PropertyChangeAbilityConstructor : RpgObjectConstructor {

    override fun construct(context: RpgObjectConstructContext): RpgObject {
        return PropertyChangeAbility<Comparable<*>>(
            context.getString(PROPERTY),
            context.getString(EXPR),
            elementId = context.elementId
        )
    }

    override fun deconstruct(context: RpgObjectDeconstructContext) {
        // do nothing
        // 属性改变并不需要被存储，它的 property 和 expr 应当在 element 描述中配置，不应被改变
    }

    companion object {
        const val PROPERTY = "property"
        const val EXPR = "expr"
    }


}

class CommonRpgModelConstructor : RpgObjectConstructor {

    override fun construct(context: RpgObjectConstructContext): RpgObject {
        return CommonRpgModel(
            elementId = context.elementId,
            meta = mapRpgData().apply {
                context.getSubDataOrNull(META)?.forEach {
                    if (it.isPrimitive()) {
                        set(it.first, it.second)
                    }
                }
            },
            abilities = context.getRpgObjectOrNull(ABILITIES)?.let {
                when(it) {
                    is RpgObjectArray -> it.filterIsInstance<RpgAbility<*, *, *, *>>()
                    is RpgAbility<*, *, *, *> -> listOf(it)
                    else -> emptyList()
                }
            } ?: emptyList()
        )
    }

    override fun deconstruct(context: RpgObjectDeconstructContext) {
        val model = context.rpgObject<CommonRpgModel>()
        context.deconstruct {
            put(META, model.meta().all())
            put(ABILITIES, model.abilities())
        }
    }

    private fun Any.isPrimitive(): Boolean {
        return this is Int || this is Long || this is Double || this is Boolean || this is Float || this is String
    }

    companion object {

        const val META = "meta"
        const val ABILITIES = "abilities"

    }

}