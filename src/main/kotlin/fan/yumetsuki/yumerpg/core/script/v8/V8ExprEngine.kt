package fan.yumetsuki.yumerpg.core.script.v8

import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Array
import com.eclipsesource.v8.V8Object
import com.eclipsesource.v8.V8Value
import fan.yumetsuki.yumerpg.core.script.ExprEngine
import fan.yumetsuki.yumerpg.core.script.ExprRuntimeContext
import fan.yumetsuki.yumerpg.core.script.ScriptSerializable
import kotlinx.serialization.json.*

/**
 * 基于 V8 的表达式执行引擎
 * @author yumetsuki
 */
class V8ExprEngine : ExprEngine {

    override fun createRuntimeContext(): ExprRuntimeContext = V8ExprRuntimeContext(V8.createV8Runtime())

}

/**
 * 基于 V8 的表达式运行时
 * @author yumetsuki
 */
class V8ExprRuntimeContext(
    private val v8: V8
): ExprRuntimeContext {

    override fun registerVariable(name: String, value: ScriptSerializable) {
        if (v8.isReleased) {
            return
        }
        when(val serializable = value.toScriptObj()) {
            is JsonPrimitive -> {
                if (serializable is JsonNull) {
                    return
                }
                serializable.doubleOrNull?.also {
                    v8.add(name, it)
                } ?: serializable.longOrNull?.toInt()?.also {
                    v8.add(name, it)
                } ?: serializable.booleanOrNull?.also {
                    v8.add(name, it)
                } ?: v8.add(name, serializable.content)
            }
            else -> v8.add(name, serializable.toV8Value())
        }
    }

    override fun exec(script: String): Any {
        if (v8.isReleased) {
            return Unit
        }
        return v8.executeScript(script)
    }

    override fun destroy() {
        v8.release()
    }

    private fun JsonElement.toV8Value() : V8Value? {
        return when(this) {
            is JsonObject -> V8Object(v8).also { obj ->
                this.forEach { k, v ->
                    obj.add(k, v.toV8Value())
                }
            }
            is JsonArray -> V8Array(v8).also { arr ->
                this.forEach {
                    arr.push(it.toV8Value())
                }
            }
            else -> null
        }
    }
}