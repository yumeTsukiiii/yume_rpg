package fan.yumetsuki.yumerpg.ecs

import kotlin.concurrent.getOrSet

object ECS {

    private val ecsManagerThreadLocal = ThreadLocal<ECSManager>()

    val current: ECSManager
        get() = ecsManagerThreadLocal.getOrSet {
            DefaultECSManager()
        }

}

interface ECSManager : ECSTicker {

    suspend fun worlds(): List<ECSWorld>

    suspend fun addWorld(world: ECSWorld)

    suspend fun getWorld(system: ECSSystem) : ECSWorld

}

class DefaultECSManager : ECSManager {

    private val worlds = mutableListOf<ECSWorld>()

    override suspend fun worlds(): List<ECSWorld> = worlds

    override suspend fun addWorld(world: ECSWorld) {
        worlds.add(world)
    }

    override suspend fun getWorld(system: ECSSystem) : ECSWorld {
        return worlds.find {
            it.systems().contains(system)
        }!!
    }

    override suspend fun onTick() {
        worlds.forEach {
            it.onTick()
        }
    }

}