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

    /**
     * 监听一个组件，当 System 在执行初始化时，若调用了 observe* 函数，则会仅在 component / entity 发生变化时执行 onUpdate
     * @see [ECSContext.update]
     */
    suspend fun <T: ECSComponent> observeComponent(componentType: KClass<T>): ECSInitializeContext

    suspend fun <T: ECSEntity> observeEntity(entityType: KClass<T>): ECSInitializeContext

}

suspend inline fun <reified T: ECSComponent> ECSInitializeContext.observeComponent(): ECSInitializeContext {
    observeComponent(T::class)
    return this
}

suspend inline fun <reified T: ECSEntity> ECSInitializeContext.observeEntity(): ECSInitializeContext {
    observeEntity(T::class)
    return this
}