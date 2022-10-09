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

    private val refVariables = mutableListOf<V8Object>()

    override fun registerVariable(name: String, value: ScriptSerializable) {
        if (v8.isReleased) {
            return
        }
        // 将 value 转化为可能的 V8Object，这是一个递归转化 Json -> 8 的过程
        // 若它直接是一个原始值（Int、Double），则直接将其添加到 v8 中
        v8.add(name, value.toScriptObj())
    }

    override fun exec(script: String): Any? {
        if (v8.isReleased) {
            return Unit
        }
        return v8.executeScript(script)
    }

    override fun destroy() {
        if (refVariables.isNotEmpty()) {
            refVariables.forEach(V8Object::release)
            refVariables.clear()
        }
        v8.release()
    }

    private fun JsonElement.toV8Value() : V8Value? {
        return when(this) {
            is JsonObject -> V8Object(v8).also { obj ->
                this.forEach { k, v ->
                    obj.add(k, v)
                }
            }
            is JsonArray -> V8Array(v8).also { arr ->
                this.forEach {
                    arr.append(it)
                }
            }
            else -> null
        }?.also(refVariables::add)
    }

    private fun V8Object.add(key: String, value: Any?) {
        when(value) {
            null -> addNull(key)
            is V8Object -> add(key, value)
            is Int -> add(key, value)
            is String -> add(key, value)
            is Double -> add(key, value)
            is Boolean -> add(key, value)
            is JsonPrimitive -> {
                if (value is JsonNull) {
                    addNull(key)
                } else {
                    value.doubleOrNull?.let {
                        add(key, it)
                    } ?: value.intOrNull?.let {
                        add(key, it)
                    } ?: value.booleanOrNull?.let {
                        add(key, it)
                    } ?: add(key, value.content)
                }
            }
            is JsonElement -> {
                add(key, value.toV8Value())
            }
        }

    }

    private fun V8Array.append(value: Any?) {
        when(value) {
            null -> pushNull()
            is V8Object -> push(value)
            is Int -> push(value)
            is String -> push(value)
            is Double -> push(value)
            is Boolean -> push(value)
            is JsonPrimitive -> {
                if (value is JsonNull) {
                    pushNull()
                } else {
                    value.doubleOrNull?.let {
                        push(it)
                    } ?: value.intOrNull?.let {
                        push(it)
                    } ?: value.booleanOrNull?.let {
                        push(it)
                    } ?: push(value.content)
                }
            }
            is JsonElement -> {
                push(value.toV8Value())
            }
        }
    }
}