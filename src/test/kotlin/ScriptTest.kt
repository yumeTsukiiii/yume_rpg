import com.eclipsesource.v8.V8ScriptExecutionException
import fan.yumetsuki.yumerpg.core.game.model.*
import fan.yumetsuki.yumerpg.core.script.ScriptSerializable
import fan.yumetsuki.yumerpg.core.script.v8.V8ExprEngine
import fan.yumetsuki.yumerpg.core.utils.rangeProperty
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ScriptTest {

    @Test
    fun testExecScript() {
        val exprEngine = V8ExprEngine()
        exprEngine.createRuntimeContext().apply {
            assertEquals(50, exec("10 + 10 * 4"), "script 执行结果不 50，屑！")
        }.destroy()
    }

    @Test
    fun testRegisterVariable() {
        val exprEngine = V8ExprEngine()
        exprEngine.createRuntimeContext().apply {
            registerVariable("owner", object : ScriptSerializable {
                override fun toScriptObj(): JsonElement {
                    return buildJsonObject {
                        putJsonObject("hp") {
                            put("value", 10)
                            put("max", 20)
                            put("min", 0)
                        }
                    }
                }
            })
            assertEquals(10, exec("owner.hp.value"))
            assertEquals(20, exec("owner.hp.max"))
            assertEquals(0, exec("owner.hp.min"))
            assertFailsWith<V8ScriptExecutionException> {
                // 未定义的变量抛出 undefined 异常
                exec("target")
            }
        }.destroy()
    }

}