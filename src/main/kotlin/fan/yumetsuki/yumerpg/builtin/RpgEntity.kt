package fan.yumetsuki.yumerpg.builtin

import fan.yumetsuki.yumerpg.ecs.ECSComponent
import fan.yumetsuki.yumerpg.ecs.ECSEntity
import fan.yumetsuki.yumerpg.serialization.RpgObject
import kotlin.reflect.KClass

interface RpgEntity : ECSEntity, RpgObject

class ListRpgEntity(
    override val name: String,
    override val elementId: Long,
    private val components: List<ECSComponent>
) : RpgEntity {

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