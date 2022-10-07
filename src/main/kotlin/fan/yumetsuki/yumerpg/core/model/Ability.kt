package fan.yumetsuki.yumerpg.core.model

/**
 * 游戏功能，代表模型本身特有的能力，比如可拥有血条，是否可被使用，可被消耗等
 * @author yumetsuki
 */
interface RpgAbility<Target> {
    /**
     * 执行该能力，为能力的处理逻辑，例如，道具的能力，使用后被作用在[RpgModel]上等
     * @param target 可选的执行目标
     */
    suspend fun execute(target: Target)
}

/**
 * 单一值逻辑的执行能力，例如使用了 HP 恢复药水，仅恢复了 HP 这一种值
 * @author yumetsuki
 */
class SingleUsable<ValueType, Target>(
    /**
     * 该能力具备的value值
     */
    private val value: ValueType,
    /**
     * 能力执行时的逻辑回调
     */
    private val executor: SingleExecutor<ValueType, Target>
): RpgAbility<Target> {

    override suspend fun execute(target: Target) = executor(value, target)

}

typealias SingleExecutor<ValueType, Target> = suspend (value: ValueType, target: Target) -> Unit

/**
 * 创建[SingleUsable]的便捷方法
 * @param value [SingleUsable.value]
 * @param executor [SingleUsable.executor]
 * @author yumetsuki
 */
fun <ValueType, Target> singleUsable(
    value: ValueType,
    executor: SingleExecutor<ValueType, Target>
) = SingleUsable(value, executor)
