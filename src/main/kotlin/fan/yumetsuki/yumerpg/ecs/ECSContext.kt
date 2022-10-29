package fan.yumetsuki.yumerpg.ecs

interface ECSContext {

    suspend fun entities(): List<RpgEntity>

    suspend fun addEntity(vararg components: RpgComponent)

    suspend fun getOwner(component: RpgComponent): RpgEntity

}