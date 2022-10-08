package fan.yumetsuki.yumerpg.core.expr

/**
 * 表达式引擎，用于执行道具，攻击等数值的计算
 * @author yumetsuki
 */
interface ExprEngine {

    /**
     * 执行脚本
     * @param context 当此执行脚本的上下文[ExprEngineContext]
     * @param script 脚本字符串
     * @return 脚本执行结果
     */
    fun exec(context: ExprEngineContext, script: String): Any

}

interface ExprEngineContext {

}