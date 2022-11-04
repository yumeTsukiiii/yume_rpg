package fan.yumetsuki.yumerpg.ecs

/**
 * System 逻辑，它将在每一次 [ECSWorld.onTick] 时触发一次[ECSSystem.onUpdate]，取决于开发者的实现，例如，每一帧回调一次
 * 注意，由于 onUpdate 可能会频繁调用，如果在其中以[ECSComponent]或者[ECSEntity]中某个值为判断条件时，请在[ECSSystem.onInitialize] 中
 * 调用[ECSInitializeContext.observeComponent]或者[ECSInitializeContext.observeEntity]
 * [ECSContext.update] 将使得监听这类 Component 和 Entity 的 System 在下一次 onTick 再回调 onUpdate
 * 否则大量的 System 都在每次 onTick 都会执行，影响性能，部分受到例如 active 条件判断的 System 不需要每次都执行
 */
interface ECSSystem {

    suspend fun onInitialize(context: ECSInitializeContext) = Unit

    suspend fun onUpdate(context: ECSContext)

}