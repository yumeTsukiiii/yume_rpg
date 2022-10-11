import fan.yumetsuki.yumerpg.core.builtin.builder.ConsumableBuilder
import fan.yumetsuki.yumerpg.core.builtin.builder.PropertyChangeAbilityBuilder
import fan.yumetsuki.yumerpg.core.serialization.RpgBuildJsonProtocol
import fan.yumetsuki.yumerpg.core.serialization.RpgBuildProtocol
import fan.yumetsuki.yumerpg.core.serialization.registerBuilder
import kotlin.test.Test


class ProtocolTest {

    @Test
    fun testJsonProtocolLoad() {
        val protocol: RpgBuildProtocol = RpgBuildJsonProtocol()
        protocol.registerBuilder(ConsumableBuilder)
        protocol.registerBuilder(PropertyChangeAbilityBuilder)
    }

}