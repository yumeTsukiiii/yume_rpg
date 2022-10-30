package fan.yumetsuki.yumerpg.builtin

import fan.yumetsuki.yumerpg.ecs.ECSSystem
import fan.yumetsuki.yumerpg.serialization.RpgObject

interface RpgSystem : ECSSystem, RpgObject {

    override val isSerializable: Boolean
        get() = false

}