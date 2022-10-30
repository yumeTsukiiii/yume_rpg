package fan.yumetsuki.yumerpg.builtin

import fan.yumetsuki.yumerpg.ecs.ECSComponent
import fan.yumetsuki.yumerpg.ecs.ECSEntity
import fan.yumetsuki.yumerpg.ecs.ListECSEntity
import fan.yumetsuki.yumerpg.serialization.RpgObject

interface RpgEntity : ECSEntity, RpgObject

class ListRpgEntity(
    name: String,
    override val elementId: Long,
    components: List<ECSComponent>
) : RpgEntity, ECSEntity by ListECSEntity(name, components)