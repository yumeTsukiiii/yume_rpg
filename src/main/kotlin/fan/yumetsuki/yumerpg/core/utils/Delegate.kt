package fan.yumetsuki.yumerpg.core.utils

import fan.yumetsuki.yumerpg.core.script.ScriptSerializable
import fan.yumetsuki.yumerpg.core.script.encodeToScriptObj
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * 表示范围的一种读写代理类，通常用于 Hp / Mp 等有最大值上限的属性
 * @author yumetsuki
 */
class RangeProperty<PropertyType: Comparable<PropertyType>>(
    private var value: PropertyType,
    var maxValue: PropertyType,
    var minValue: PropertyType
) : ReadWriteProperty<Any?, PropertyType>, ScriptSerializable {

    override fun getValue(thisRef: Any?, property: KProperty<*>): PropertyType {
        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: PropertyType) {
        this.value = maxOf(minOf(value, maxValue), minValue)
    }

    override fun toScriptObj(): JsonElement = buildJsonObject {
        put("value", value.encodeToScriptObj())
        put("max", maxValue.encodeToScriptObj())
        put("min", minValue.encodeToScriptObj())
    }

}

/**
 * 创建一个[RangeProperty]
 * @param value 当前属性值
 * @param maxValue 属性最大值
 * @param minValue 属性最小值
 * @return [RangeProperty]
 */
inline fun <reified PropertyType: Comparable<PropertyType>> rangeProperty(
    value: PropertyType, maxValue: PropertyType, minValue: PropertyType
) = RangeProperty(value, maxValue, minValue)