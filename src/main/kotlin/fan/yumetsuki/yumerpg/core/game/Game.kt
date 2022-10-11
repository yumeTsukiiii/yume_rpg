package fan.yumetsuki.yumerpg.core.game

import fan.yumetsuki.yumerpg.core.script.ScriptEngine
import fan.yumetsuki.yumerpg.core.script.ScriptExecutor
import fan.yumetsuki.yumerpg.core.script.ScriptSerializable
import fan.yumetsuki.yumerpg.core.script.v8.V8ScriptEngine

/**
 * 游戏引擎，代表当前游戏中整个运行环境，全局透出对外 API
 * @author yumetsuki
 */
interface GameEngine : ScriptExecutor

/**
 * 全局获取 gameManager 对象
 */
val gameEngine : GameEngine
    // TODO 测试用的 GameManager，记得删除
    get() = object : GameEngine {

        val scriptEngine : ScriptEngine = V8ScriptEngine()

        @Suppress("UNCHECKED_CAST")
        override fun <T> execScript(variables: Map<String, ScriptSerializable>, script: String): T {
            return scriptEngine.createRuntimeContext().run {
                variables.forEach(this::registerVariable)
                (exec(script) as T).apply {
                    destroy()
                }
            }
        }

        override fun <T> execScript(script: String): T = execScript(mapOf(), script)

    }