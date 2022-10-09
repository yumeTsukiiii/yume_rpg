package fan.yumetsuki.yumerpg.core.script

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/**
 * 表达式引擎，用于执行道具，攻击等数值的计算
 * @author yumetsuki
 */
interface ExprEngine {

    /**
     * 创建执行上下文
     * @return [ExprRuntimeContext]
     */
    fun createRuntimeContext(): ExprRuntimeContext

}

/**
 * 脚本执行时的上下文
 * @author yumetsuki
 */
interface ExprRuntimeContext {

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

fun Any?.encodeToScriptObj(): JsonElement = when(this) {
    is Number -> JsonPrimitive(this)
    is String -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is ScriptSerializable -> toScriptObj()
    else -> JsonNull
}