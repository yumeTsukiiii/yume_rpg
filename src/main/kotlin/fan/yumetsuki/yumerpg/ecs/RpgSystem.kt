package fan.yumetsuki.yumerpg.ecs

interface RpgSystem {

    suspend fun onUpdate(context: ECSContext)

}