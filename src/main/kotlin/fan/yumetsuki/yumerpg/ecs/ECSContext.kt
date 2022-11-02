package fan.yumetsuki.yumerpg.ecs

import kotlin.reflect.KClass

interface ECSContext {

    suspend fun entities(): List<ECSEntity>

    suspend fun addEntity(vararg entities: ECSEntity)

    suspend fun getOwner(component: ECSComponent): ECSEntity

    suspend fun update(vararg components: ECSComponent)

    suspend fun update(vararg entities: ECSEntity)

}

suspend fun ECSContext.entitiesComponents() : List<ECSComponent> = entities().flatMap { it.components() }


interface ECSInitializeContext {

    suspend fun ECSObservableSystem.observeComponents(vararg componentTypes: KClass<ECSComponent>)

    suspend fun ECSObservableSystem.observeEntities(vararg entityTypes: KClass<ECSEntity>)

}