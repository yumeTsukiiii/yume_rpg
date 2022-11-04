package fan.yumetsuki.yumerpg.builtin.rpgobject

import fan.yumetsuki.yumerpg.builtin.RpgComponent
import fan.yumetsuki.yumerpg.builtin.RpgSystem
import fan.yumetsuki.yumerpg.ecs.*

class ConsumableComponent(
    override val elementId: Long,
    var count: Long = 0,
    var isSelect: Boolean = false,
    var useCount: Long = 0,
    var user: ECSEntity? = null,
    var target: ECSEntity? = null
) : RpgComponent {

    override val identify: String
        get() = "Consumable"

}

fun ConsumableComponent.isActive(): Boolean {
    return isSelect && user != null && target != null
}

fun ConsumableComponent.deactivate() {
    user = null
    target = null
    useCount = 0
}

fun ConsumableComponent.select() {
    isSelect = true
}

fun ConsumableComponent.unselect() {
    isSelect = false
}

fun ConsumableComponent.consume(user: ECSEntity, target: ECSEntity, useCount: Long = 1) {
    if (count <= 0 || useCount <= 0 || !isSelect) {
        return
    }
    this.useCount = useCount
    this.user = user
    this.target = target
}

class ConsumableSystem(
    override val elementId: Long
) : RpgSystem {

    override suspend fun onInitialize(context: ECSInitializeContext) {
        context.observeComponent<ConsumableComponent>()
    }

    override suspend fun onUpdate(context: ECSContext) {
        context.entitiesComponents().filterIsInstance<ConsumableComponent>().filter {
            it.isActive()
        }.map {
            it to context.getOwner(it).components().filterIsInstance<EffectComponent>()
        }.forEach { (consumable, effects) ->
            while (consumable.useCount > 0) {
                effects.forEach {
                    it.activateAndUpdate(context, consumable.user!!, consumable.target!!)
                }
                consumable.useCount--
                consumable.count--
            }
        }
    }

}