package fan.yumetsuki.yumerpg.builtin

import fan.yumetsuki.yumerpg.serialization.RpgObject

/**
 * 游戏功能，代表模型本身特有的能力，比如可拥有血条，是否可被使用，可被消耗等
 * @author yumetsuki
 */
interface RpgAbility<Owner, Target, Param, Result> : RpgObject {

    /**
     * 能力名称
     */
    val name: String

    /**
     * 能力别名，用于备注的 name
     */
    val alias: String?
        get() = null

    /**
     * 执行该能力，为能力的处理逻辑，例如，道具的能力，使用后被作用在[RpgModel]上等
     * @param target 可选的执行目标
     */
    suspend fun execute(owner: Owner, target: Target, param: Param): Result
}

/**
 * 属性能力，持有该能力的对象将具备 value 值的属性，执行返回该属性的值
 * @author yumetsuki
 */
interface PropertyAbility<PropertyType, Owner>: RpgAbility<Owner, Unit, Unit, PropertyType> {

    var value: PropertyType

    override suspend fun execute(owner: Owner, target: Unit, param: Unit) = value

}

/**
 * 拥有上下限值的属性能力，它的[value]属性在赋值时，必须在[minValue]和[maxValue]的范围内
 */
interface RangePropertyAbility<PropertyType: Comparable<PropertyType>, Owner>: PropertyAbility<PropertyType, Owner> {
    var maxValue: PropertyType
    var minValue: PropertyType
}

/**
 * 指令能力，无执行返回值的能力，通常用于仅对 Target 的执行逻辑，例如攻击削减 Target 的生命值
 * @author yumetsuki
 */
interface CommandAbility<Owner, Target, Param> : RpgAbility<Owner, Target, Param, Unit>

/**
 * 不带参数执行的指令能力
 * @author yumetsuki
 */
interface NoParamCommandAbility<Owner, Target> : CommandAbility<Owner, Target, Unit> {

    suspend fun execute(owner: Owner, target: Target)
    override suspend fun execute(owner: Owner, target: Target, param: Unit) = execute(owner, target)

}