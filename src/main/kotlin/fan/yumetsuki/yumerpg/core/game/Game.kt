package fan.yumetsuki.yumerpg.core.game

import fan.yumetsuki.yumerpg.core.script.ExprEngine
import fan.yumetsuki.yumerpg.core.script.ScriptSerializable
import fan.yumetsuki.yumerpg.core.script.v8.V8ExprEngine

/**
 * 游戏引擎，代表当前游戏中整个运行环境，全局透出对外 API
 * @author yumetsuki
 */
interface GameEngine {

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

/**
 * 全局获取 gameManager 对象
 */
val gameEngine : GameEngine
    // TODO 测试用的 GameManager，记得删除
    get() = object : GameEngine {

        val exprEngine : ExprEngine = V8ExprEngine()

        @Suppress("UNCHECKED_CAST")
        override fun <T> execScript(variables: Map<String, ScriptSerializable>, script: String): T {
            return exprEngine.createRuntimeContext().run {
                variables.forEach(this::registerVariable)
                (exec(script) as T).apply {
                    destroy()
                }
            }
        }

        override fun <T> execScript(script: String): T = execScript(mapOf(), script)

    }