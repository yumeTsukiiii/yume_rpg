package fan.yumetsuki.yumerpg.ecs

interface ECSSystem {

    suspend fun onInitialize(context: ECSInitializeContext) = Unit

    suspend fun onUpdate(context: ECSContext)

}

interface ECSObservableSystem : ECSSystem