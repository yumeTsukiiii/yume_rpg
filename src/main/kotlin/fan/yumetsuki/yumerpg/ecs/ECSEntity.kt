package fan.yumetsuki.yumerpg.ecs

import kotlin.reflect.KClass

interface ECSEntity {

    val name: String

    suspend fun components(): List<ECSComponent>

    suspend fun <T: ECSComponent> componentOrNull(clazz: KClass<T>): ECSComponent?

    suspend fun <T: ECSComponent> hasComponent(clazz: KClass<T>): Boolean

}

class ListECSEntity(
    override val name: String,
    private val components: List<ECSComponent>
): ECSEntity {

    override suspend fun components(): List<ECSComponent> = components

    override suspend fun <T: ECSComponent> componentOrNull(clazz: KClass<T>): ECSComponent? {
        return components().find {
            clazz.isInstance(it)
        }
    }

    override suspend fun <T : ECSComponent> hasComponent(clazz: KClass<T>): Boolean {
        return componentOrNull(clazz) != null
    }


}

suspend inline fun <reified T: ECSComponent> ECSEntity.componentOrNull(): ECSComponent? {
    return componentOrNull(T::class)
}

suspend inline fun <reified T: ECSComponent> ECSEntity.component(): ECSComponent {
    return componentOrNull<T>()!!
}

suspend inline fun <reified T: ECSComponent> ECSEntity.hasComponent(): Boolean {
    return hasComponent(T::class)
}