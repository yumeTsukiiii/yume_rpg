package fan.yumetsuki.yumerpg.core.game.model

import fan.yumetsuki.yumerpg.core.model.BaseRpgModel
import fan.yumetsuki.yumerpg.core.model.RpgModel
import fan.yumetsuki.yumerpg.core.model.mapMeta

class RpgCharacter(
    name: String,
    description: String
): RpgModel by BaseRpgModel(
    mapMeta(
        "name" to name,
        "description" to description
    ),
    listOf(

    )
)
