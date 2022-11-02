package fan.yumetsuki.yumerpg.ecs

import kotlin.reflect.KClass

interface ECSWorld : ECSContext, ECSTicker {

    suspend fun systems(): Set<ECSSystem>

    suspend fun addSystem(vararg systems: ECSSystem)

}

class SimpleECSWorld : ECSWorld {

    private val systems = mutableSetOf<ECSSystem>()
    private val observableSystems = mutableMapOf<KClass<*>, MutableSet<ECSSystem>>()

    private val entities = mutableListOf<ECSEntity>()

    private val nextTicks = mutableListOf<ECSSystem>()

    private val simpleInitializeContext = SimpleECSInitializeContext()

    override suspend fun addSystem(vararg systems: ECSSystem) {

        systems.filter {
            it !in systems
        }.forEach {
            simpleInitializeContext.currentSystem = it
            it.onInitialize(simpleInitializeContext)
        }

        simpleInitializeContext.observableSystems.forEach { (k, v) ->
            observableSystems[k] = v
        }

        this.systems.addAll(
            systems.subtract(
                simpleInitializeContext.observableSystems.values.flatten().toSet()
            )
        )

    }

    override suspend fun systems(): Set<ECSSystem> {
        return systems
    }

    override suspend fun onTick() {
        // 执行一轮延时执行的 System 任务
        if (nextTicks.isNotEmpty()) {
            buildList {
                addAll(nextTicks)
            }.also { snapshot ->
                nextTicks.clear()
                snapshot.forEach {
                    it.onUpdate(this)
                }
            }
        }
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

    override suspend fun update(vararg components: ECSComponent) {
        updateECSItem(*components)
    }

    override suspend fun update(vararg entities: ECSEntity) {
        updateECSItem(*entities)
    }

    private fun updateECSItem(vararg ecsItem: Any) {
        nextTicks.addAll(
            ecsItem.mapNotNull {
                observableSystems[it::class]
            }.flatten()
        )
    }

    class SimpleECSInitializeContext: ECSInitializeContext {

        lateinit var currentSystem: ECSSystem

        val observableSystems = mutableMapOf<KClass<*>, MutableSet<ECSSystem>>()

        override suspend fun <T : ECSComponent> observeComponent(componentType: KClass<T>): ECSInitializeContext {
            currentSystem.observeECSItem(componentType)
            return this
        }

        override suspend fun <T : ECSEntity> observeEntity(entityType: KClass<T>): ECSInitializeContext {
            currentSystem.observeECSItem(entityType)
            return this
        }

        private fun ECSSystem.observeECSItem(vararg ecsItemTypes: KClass<*>) {
            ecsItemTypes.forEach { type ->
                observableSystems.getOrPut(type) {
                    mutableSetOf()
                }.add(this)
            }
        }
    }
}