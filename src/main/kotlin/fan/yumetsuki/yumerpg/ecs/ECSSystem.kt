package fan.yumetsuki.yumerpg.ecs

interface ECSSystem {

    suspend fun onUpdate(context: ECSContext)

}