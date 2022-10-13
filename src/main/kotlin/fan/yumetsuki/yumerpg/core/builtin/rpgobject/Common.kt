package fan.yumetsuki.yumerpg.core.builtin.rpgobject

import fan.yumetsuki.yumerpg.core.builtin.*
import fan.yumetsuki.yumerpg.core.script.ScriptExecutor
import fan.yumetsuki.yumerpg.core.serialization.*
import fan.yumetsuki.yumerpg.core.utils.RangeProperty
import fan.yumetsuki.yumerpg.core.utils.putIfIsInstance

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
class ScriptAbility<Owner, Target, Param, Result>(
    private val scriptExecutor: ScriptExecutor,
    override val name: String = "Script",
    override val elementId: Long
) : RpgAbility<Owner, Target, ScriptAbility.ScriptParam<Param>, Result> {

    override suspend fun execute(owner: Owner, target: Target, param: ScriptParam<Param>): Result {
        return scriptExecutor.execScript(buildMap {
            putIfIsInstance("owner", owner)
            putIfIsInstance("target", owner)
            putIfIsInstance("param", param.param)
        }, param.script)
    }

    class ScriptParam<P>(
        val script: String,
        val param: P
    )
}

/**
 * 仅传递 script 字符串的[ScriptAbility]执行方法，参数可选
 */
suspend fun <Owner, Target, Result> ScriptAbility<Owner, Target, Any?, Result>.execute(
    owner: Owner, target: Target, script: String, param: Any? = null
): Result {
    return execute(owner, target, ScriptAbility.ScriptParam(script, param))
}

/**
 * 执行脚本的便捷方法
 */
suspend fun <Target, Result> RpgModel.execScript(
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