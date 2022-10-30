package fan.yumetsuki.yumerpg.builtin.rpgobject

import fan.yumetsuki.yumerpg.builtin.RpgComponent
import fan.yumetsuki.yumerpg.builtin.RpgSystem
import fan.yumetsuki.yumerpg.builtin.script.ScriptEngine
import fan.yumetsuki.yumerpg.builtin.script.v8.V8ScriptEngine
import fan.yumetsuki.yumerpg.builtin.script.v8.registerECSEntity
import fan.yumetsuki.yumerpg.ecs.ECSContext
import fan.yumetsuki.yumerpg.ecs.ECSEntity
import fan.yumetsuki.yumerpg.ecs.entitiesComponents
import fan.yumetsuki.yumerpg.serialization.*

class PropertyComponent<PropertyType: Any>(
    override val elementId: Long,
    var value: PropertyType,
    val name: String,
    val alias: String? = null,
) : RpgComponent {

    override val identify: String
        get() = name

}

typealias NumberPropertyComponent = PropertyComponent<Number>

typealias BooleanPropertyComponent = PropertyComponent<Boolean>

typealias StringPropertyComponent = PropertyComponent<String>

class PropertyChangeComponent(
    override val elementId: Long,
    val changedProperty: String,
    val script: String,
    override var user: ECSEntity? = null,
    override var target: ECSEntity? = null
) : EffectComponent {

    override val identify: String
        get() = "PropertyChange-$changedProperty"

    override val isSerializable: Boolean = false

}

class PropertyChangeSystem(
    override val elementId: Long,
    private val scriptEngine: ScriptEngine
) : RpgSystem {

    @Suppress("UNCHECKED_CAST")
    override suspend fun onUpdate(context: ECSContext) {
        context.entitiesComponents().filterIsInstance<PropertyChangeComponent>().filter {
            it.isActive() && context.getOwner(it) == it.target
        }.forEach {
            it.target!!.components().filterIsInstance<PropertyComponent<*>>().find { property ->
                property.name == it.changedProperty
            }?.also { property ->
                scriptEngine.createRuntimeContext().apply {
                    registerECSEntity("user", it.user!!)
                    registerECSEntity("target", it.target!!)
                    exec(it.script)?.also { result ->
                        when(property.value) {
                            is Number -> {
                                (property as NumberPropertyComponent).value = result as Number
                            }
                            is String -> {
                                (property as StringPropertyComponent).value = result as String
                            }
                            is Boolean -> {
                                (property as BooleanPropertyComponent).value = result as Boolean
                            }
                        }
                    }
                    // 执行过一次能力后，重置执行状态
                    it.deactivate()
                }.destroy()
            }
        }
    }

}

/**
 * 属性组件构建器，通常用于给某个角色添加可变的属性值，例如可变的角色名称
 * @author yumetsuki
 */
class PropertyComponentConstructor : RpgObjectConstructor {

    override suspend fun construct(context: RpgObjectConstructContext): RpgObject {
        val type = context.getString(TYPE)
        val name = context.getString(NAME)
        val alias = context.getStringOrNull(ALIAS)
        return when(type) {
            "number" -> NumberPropertyComponent(context.elementId, context.getDoubleOrNull(VALUE) ?: 0, name, alias)
            "string" -> StringPropertyComponent(context.elementId, context.getStringOrNull(VALUE) ?: "", name, alias)
            "boolean" -> BooleanPropertyComponent(context.elementId, context.getBooleanOrNull(VALUE) ?: false, name, alias)
            else -> error("不支持的属性类型 $type")
        }
    }

    override suspend fun deconstruct(context: RpgObjectDeconstructContext) {
        context.deconstruct {
            context.rpgObject<PropertyComponent<*>>().apply {
                put(VALUE, value)
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
 * 拥有范围限制的属性组件，拥有一个可被改变的值[value]
 * 并且 value 在被赋值时，不会超出[minValue]和[maxValue]的范围
 * @author yumetsuki
 */
class RangePropertyComponent<PropertyType: Comparable<PropertyType>>(
    override val elementId: Long,
    private var internalValue : PropertyType,
    var minValue: PropertyType,
    var maxValue: PropertyType,
    val name: String,
    val alias: String? = null,
) : RpgComponent {

    var value: PropertyType
        get() = internalValue
        set(value) {
            this.internalValue = maxOf(minOf(value, maxValue), minValue)
        }

}

typealias LongRangePropertyAbility = RangePropertyComponent<Long>

typealias DoubleRangePropertyAbility = RangePropertyComponent<Double>

typealias BooleanRangePropertyAbility = RangePropertyComponent<Boolean>

typealias StringRangePropertyAbility = RangePropertyComponent<String>

class PropertyChangeComponentConstructor : RpgObjectConstructor {

    override suspend fun construct(context: RpgObjectConstructContext): RpgObject {

        return PropertyChangeComponent(
            elementId = context.elementId,
            context.getString(CHANGED_PROPERTY),
            context.getString(SCRIPT)
        )
    }

    companion object {
        const val CHANGED_PROPERTY = "changedProperty"
        const val SCRIPT = "script"
    }

}

class PropertyChangeSystemConstructor : RpgObjectConstructor {

    override suspend fun construct(context: RpgObjectConstructContext): RpgObject {
        return PropertyChangeSystem(
            context.elementId,
            V8ScriptEngine
        )
    }

}