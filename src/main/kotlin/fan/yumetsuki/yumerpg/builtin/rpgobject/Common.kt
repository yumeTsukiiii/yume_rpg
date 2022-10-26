package fan.yumetsuki.yumerpg.builtin.rpgobject

import fan.yumetsuki.yumerpg.builtin.*
import fan.yumetsuki.yumerpg.builtin.script.ScriptEngine
import fan.yumetsuki.yumerpg.builtin.script.v8.V8ScriptEngine
import fan.yumetsuki.yumerpg.builtin.script.v8.registerRpgModel
import fan.yumetsuki.yumerpg.serialization.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * 属性能力，拥有一个可被改变的值[value]
 * @author yumetsuki
 */
class CommonPropertyAbility<PropertyType>(
    override var value: PropertyType,
    override val name: String,
    override val alias: String? = null,
    override val elementId: Long
) : PropertyAbility<PropertyType, RpgModel>

typealias NumberPropertyAbility = CommonPropertyAbility<Number>

typealias BooleanPropertyAbility = CommonPropertyAbility<Boolean>

typealias StringPropertyAbility = CommonPropertyAbility<String>

/**
 * 拥有范围限制的属性能力，拥有一个可被改变的值[value]
 * 并且 value 在被赋值时，不会超出[minValue]和[maxValue]的范围
 * @author yumetsuki
 */
class CommonRangePropertyAbility<PropertyType: Comparable<PropertyType>>(
    private val rangeValue: RangeProperty<PropertyType>,
    override val name: String,
    override val alias: String? = null,
    override val elementId: Long
) : RangePropertyAbility<PropertyType, RpgModel> {

    override var value: PropertyType by rangeValue
    override var maxValue: PropertyType
        get() = rangeValue.maxValue
        set(value) { rangeValue.maxValue = value }
    override var minValue: PropertyType
        get() = rangeValue.minValue
        set(value) { rangeValue.minValue = value }

}

typealias LongRangePropertyAbility = CommonRangePropertyAbility<Long>

typealias DoubleRangePropertyAbility = CommonRangePropertyAbility<Double>

typealias BooleanRangePropertyAbility = CommonRangePropertyAbility<Boolean>

typealias StringRangePropertyAbility = CommonRangePropertyAbility<String>


/**
 * 持有能力，表示[RpgModel]持有其他的对象，并且这个被持有的对象集归属为一类属性
 * @author yumetsuki
 */
class HoldAbility(
    override var value: List<RpgModel>,
    override val name: String,
    override val alias: String? = null,
    override val elementId: Long
) : PropertyAbility<List<RpgModel>, RpgModel>

/**
 * 脚本执行能力，通常用于 HP 回复等计算表达式执行
 * @author yumetsuki
 */
class ScriptAbility<Owner: Any, Target: Any, Param: Any?, Result>(
    private val scriptEngine: ScriptEngine = V8ScriptEngine,
    override val name: String = "Script",
    override val elementId: Long
) : RpgAbility<Owner, Target, ScriptAbility.ScriptParam<Param>, Result> {

    @Suppress("UNCHECKED_CAST")
    override suspend fun execute(owner: Owner, target: Target, param: ScriptParam<Param>): Result {
        return scriptEngine.createRuntimeContext().run {
            if (owner is RpgModel) {
                registerRpgModel("owner", owner)
            } else {
                registerVariable("owner", owner)
            }
            if (target is RpgModel) {
                registerRpgModel("target", target)
            } else {
                registerVariable("target", target)
            }
            if (param.param != null) {
                registerVariable("param", param.param)
            }
            exec(param.script).apply {
                destroy()
            }
        } as Result
    }

    class ScriptParam<P>(
        val script: String,
        val param: P
    )
}

/**
 * 仅传递 script 字符串的[ScriptAbility]执行方法，参数可选
 */
suspend fun <Owner: Any, Target: Any, Result> ScriptAbility<Owner, Target, Any?, Result>.execute(
    owner: Owner, target: Target, script: String, param: Any? = null
): Result {
    return execute(owner, target, ScriptAbility.ScriptParam(script, param))
}

/**
 * 执行脚本的便捷方法
 */
suspend fun <Target: Any, Result> RpgModel.execScript(
    target: Target, script: String, param: Any? = null
): Result? {
    return execAbility<ScriptAbility<RpgModel, Target, Any?, Result>, _, _, _>(
        target, ScriptAbility.ScriptParam(script, param)
    )
}

/**
 * 改变对象属性值的能力，通常用于恢复技能/药水
 * @author yumetsuki
 */
class PropertyChangeAbility<PropertyType>(
    /**
     * 需要改变的属性名
     */
    private val property: String,
    /**
     * 计算属性值的表达式，由表达式引擎能力执行
     */
    private val expr: String,
    override val name: String = "PropertyChange",
    override val alias: String? = "用于改变属性值",
    override val elementId: Long
) : NoParamCommandAbility<RpgModel, RpgModel> {

    override suspend fun execute(owner: RpgModel, target: RpgModel) {
        target.changeProperty<PropertyType>(property) {
            owner.execScript(target, expr) ?: it
        }
    }

}

typealias NumberPropertyChangeAbility = PropertyChangeAbility<Number>

typealias BooleanPropertyChangeAbility = PropertyChangeAbility<Boolean>

typealias StringPropertyChangeAbility = PropertyChangeAbility<String>

/**
 * 改变对象属性值上下限的能力，通常用于 HP、MP 等属性值
 * @author yumetsuki
 */
class RangePropertyChangeAbility<PropertyType: Comparable<PropertyType>>(
    /**
     * 需要改变的属性名
     */
    private val property: String,
    /**
     * 计算属性最大值值的表达式，由表达式引擎能力执行
     */
    private val maxValueExpr: String? = null,
    /**
     * 计算属性最小值的表达式，由表达式引擎能力执行
     */
    private val minValueExpr: String? = null,
    override val name: String = "RangePropertyChange",
    override val alias: String? = "用于改变带范围的属性值",
    override val elementId: Long
) : NoParamCommandAbility<RpgModel, RpgModel> {

    override suspend fun execute(owner: RpgModel, target: RpgModel) {
        maxValueExpr?.also { expr ->
            target.changeRangePropertyMax<PropertyType>(property) {
                owner.execScript(target, expr) ?: it
            }
        }
        minValueExpr?.also { expr ->
            target.changeRangePropertyMin<PropertyType>(property) {
                owner.execScript(target, expr) ?: it
            }
        }
    }

}

typealias IntRangePropertyChangeAbility = RangePropertyChangeAbility<Int>

typealias DoubleRangePropertyChangeAbility = RangePropertyChangeAbility<Double>

typealias BooleanRangePropertyChangeAbility = RangePropertyChangeAbility<Boolean>

typealias StringRangePropertyChangeAbility = RangePropertyChangeAbility<String>

/**
 * 表示范围的一种读写代理类，通常用于 Hp / Mp 等有最大值上限的属性
 * @author yumetsuki
 */
class RangeProperty<PropertyType: Comparable<PropertyType>>(
    private var value: PropertyType,
    var maxValue: PropertyType,
    var minValue: PropertyType
) : ReadWriteProperty<Any?, PropertyType> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): PropertyType {
        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: PropertyType) {
        this.value = maxOf(minOf(value, maxValue), minValue)
    }

}