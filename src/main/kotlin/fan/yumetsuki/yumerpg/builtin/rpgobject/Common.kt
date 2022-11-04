package fan.yumetsuki.yumerpg.builtin.rpgobject

import fan.yumetsuki.yumerpg.builtin.*
import fan.yumetsuki.yumerpg.ecs.*
import kotlin.reflect.KClass

interface EffectComponent : RpgComponent {

    var user: ECSEntity?

    var target: ECSEntity?

}

fun EffectComponent.isActive(): Boolean {
    return user != null && target != null
}

val EffectComponent.activeUser
    get() = if (isActive()) user!! else error("该组件未被激活")

val EffectComponent.activeTarget
    get() = if (isActive()) target!! else error("该组件未被激活")

fun EffectComponent.deactivate() {
    user = null
    target = null
}

fun EffectComponent.activate(user: ECSEntity, target: ECSEntity) {
    this.user = user
    this.target = target
}

suspend fun EffectComponent.activateAndUpdate(context: ECSContext, user: ECSEntity, target: ECSEntity) {
    activate(user, target)
    context.update(this)
}

interface EffectSystem<T: EffectComponent>: RpgSystem {

    val effectComponentClass: KClass<T>

    override suspend fun onInitialize(context: ECSInitializeContext) {
        context.observeComponent(effectComponentClass)
    }

    override suspend fun onUpdate(context: ECSContext) {
        context.entitiesComponents().filterIsInstance(effectComponentClass.java).filter {
            it.isActive()
        }.forEach {
            onEffectActive(it)
            it.deactivate()
        }
    }

    suspend fun onEffectActive(effectComponent: T)

}