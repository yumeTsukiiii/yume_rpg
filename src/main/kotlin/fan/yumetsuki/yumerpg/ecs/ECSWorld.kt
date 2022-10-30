package fan.yumetsuki.yumerpg.ecs

interface ECSWorld : ECSContext {

    suspend fun addSystem(vararg systems: ECSSystem)

    suspend fun onTick()

}

class SimpleECSWorld : ECSWorld {

    private val systems = mutableListOf<ECSSystem>()
    private val entities = mutableListOf<ECSEntity>()

    override suspend fun addSystem(vararg systems: ECSSystem) {
        this.systems.addAll(systems.toList())
    }

    override suspend fun onTick() {
        systems.forEach {
            it.onUpdate(this)
        }
    }

    override suspend fun entities(): List<ECSEntity> = entities

    override suspend fun addEntity(vararg entities: ECSEntity) {
        this.entities.addAll(entities)
    }

    override suspend fun getOwner(component: ECSComponent): ECSEntity = entities().find {
        it.components().contains(component)
    }!!

}