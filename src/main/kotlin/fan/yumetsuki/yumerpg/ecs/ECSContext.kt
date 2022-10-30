package fan.yumetsuki.yumerpg.ecs

interface ECSContext {

    suspend fun entities(): List<ECSEntity>

    suspend fun addEntity(vararg components: ECSComponent)

    suspend fun getOwner(component: ECSComponent): ECSEntity

}

suspend fun ECSContext.entitiesComponents() : List<ECSComponent> = entities().flatMap { it.components() }