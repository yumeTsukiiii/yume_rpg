package fan.yumetsuki.yumerpg.builtin.script

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
    fun registerVariable(name: String, value: Any)

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