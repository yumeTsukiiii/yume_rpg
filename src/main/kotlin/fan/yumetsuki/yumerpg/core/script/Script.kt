package fan.yumetsuki.yumerpg.core.script

import fan.yumetsuki.yumerpg.core.game.GameEngine
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/**
 * 表达式引擎，用于执行道具，攻击等数值的计算
 * @author yumetsuki
 */
interface ScriptEngine {

    /**
     * 创建执行上下文
     * @return [ScriptRuntimeContext]
     */
    fun createRuntimeContext(): ScriptRuntimeContext

}

/**
 * 脚本执行时的上下文
 * @author yumetsuki
 */
interface ScriptRuntimeContext {

    /**
     * 注册变量，该变量在脚本中可以全局访问
     * @param name 变量名
     * @param value 变量值
     */
    fun registerVariable(name: String, value: ScriptSerializable)

    /**
     * 执行脚本
     * @param script 脚本字符串
     * @return 脚本执行结果
     */
    fun exec(script: String): Any?

    /**
     * 销毁上下文，销毁后则无法再执行脚本
     */
    fun destroy()

}

interface ScriptSerializable {

    fun toScriptObj(): JsonElement

}

interface ScriptExecutor {

    /**
     * 执行脚本
     * @param variables 表达式中的全局变量
     * @param script 被执行的脚本字符串
     */
    fun <T> execScript(variables: Map<String, ScriptSerializable>, script: String) : T

    /**
     * @see GameEngine.execScript
     * @param script 被执行的脚本字符串
     */
    fun <T> execScript(script: String) : T

}

fun Any?.encodeToScriptObj(): JsonElement = when(this) {
    is Number -> JsonPrimitive(this)
    is String -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is ScriptSerializable -> toScriptObj()
    else -> JsonNull
}