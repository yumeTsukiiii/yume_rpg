import fan.yumetsuki.yumerpg.core.game.builder.ConsumableBuilder
import fan.yumetsuki.yumerpg.core.game.builder.PropertyChangeAbilityBuilder
import fan.yumetsuki.yumerpg.core.protocol.RpgBuildJsonProtocol
import fan.yumetsuki.yumerpg.core.protocol.RpgBuildProtocol
import fan.yumetsuki.yumerpg.core.protocol.registerBuilder
import kotlin.test.Test


class ProtocolTest {

    @Test
    fun testJsonProtocolLoad() {
        val protocol: RpgBuildProtocol = RpgBuildJsonProtocol()
        protocol.registerBuilder(ConsumableBuilder)
        protocol.registerBuilder(PropertyChangeAbilityBuilder)
    }

}