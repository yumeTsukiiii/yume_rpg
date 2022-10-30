package fan.yumetsuki.yumerpg.builtin.rpgobject

import fan.yumetsuki.yumerpg.builtin.*
import fan.yumetsuki.yumerpg.ecs.ECSEntity

interface EffectComponent : RpgComponent {

    var user: ECSEntity?

    var target: ECSEntity?

}

fun EffectComponent.isActive(): Boolean {
    return user != null && target != null
}

fun EffectComponent.deactivate() {
    user = null
    target = null
}

fun EffectComponent.activate(user: ECSEntity, target: ECSEntity) {
    this.user = user
    this.target = target
}