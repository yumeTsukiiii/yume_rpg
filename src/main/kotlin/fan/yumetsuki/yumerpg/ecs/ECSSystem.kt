package fan.yumetsuki.yumerpg.ecs

/**
 * System 逻辑，它将在每一次 [ECSWorld.onTick] 时触发一次[ECSSystem.onUpdate]，取决于开发者的实现，例如，每一帧回调一次
 * 注意，由于 onUpdate 可能会频繁调用，如果在其中以[ECSComponent]或者[ECSEntity]中某个值为判断条件时，请在[ECSSystem.onInitialize] 中
 * 调用[ECSInitializeContext.observeComponent]或者[ECSInitializeContext.observeEntity]
 * [ECSContext.update] 将使得监听这类 Component 和 Entity 的 System 在下一次 onTick 再回调 onUpdate
 * 否则有可能在一次 onTick 中先执行的 System 使得后面很多 System 条件判断受影响一并执行
 * 执行过长逻辑导致卡死，例如每一帧中执行了一个很长的动画
 * TODO 重新考虑这个设计，是否需要在每次 onTick 时，创建 Entity / Component 的 SnapShot，对它们的更改，延迟到下一个 onTick 生效
 */
interface ECSSystem {

    suspend fun onInitialize(context: ECSInitializeContext) = Unit

    suspend fun onUpdate(context: ECSContext)

}