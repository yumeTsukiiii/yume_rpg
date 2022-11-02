package fan.yumetsuki.yumerpg.ecs

import kotlin.reflect.KClass

interface ECSWorld : ECSContext, ECSTicker {

    suspend fun systems(): List<ECSSystem>

    suspend fun addSystem(vararg systems: ECSSystem)

}

class SimpleECSWorld : ECSWorld, ECSInitializeContext {

    private val systems = mutableListOf<ECSSystem>()
    private val observableSystems = mutableMapOf<KClass<*>, MutableSet<ECSObservableSystem>>()

    private val entities = mutableListOf<ECSEntity>()

    override suspend fun addSystem(vararg systems: ECSSystem) {

        systems.forEach {
            it.onInitialize(this)
        }

        val observableSystems = systems.filterIsInstance<ECSObservableSystem>().toSet()

        this.systems.addAll(
            systems.subtract(observableSystems)
        )

    }

    override suspend fun systems(): List<ECSSystem> {
        return systems
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

    override suspend fun ECSObservableSystem.observeComponents(vararg componentTypes: KClass<ECSComponent>) {
        componentTypes.forEach { type ->
            observableSystems.getOrPut(type) {
                mutableSetOf()
            }.add(this)
        }
    }

    override suspend fun ECSObservableSystem.observeEntities(vararg entityTypes: KClass<ECSEntity>) {

    }

    override suspend fun update(vararg components: ECSComponent) {
        TODO("Not yet implemented")
    }

    override suspend fun update(vararg entities: ECSEntity) {
        TODO("Not yet implemented")
    }

}