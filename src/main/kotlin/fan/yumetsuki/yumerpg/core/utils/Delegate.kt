package fan.yumetsuki.yumerpg.core.utils

import fan.yumetsuki.yumerpg.core.script.ScriptSerializable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Serializable
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

    override fun toScriptObj(): JsonElement {
        return Json.encodeToJsonElement(this)
    }

}

inline fun <reified PropertyType: Comparable<PropertyType>> rangeProperty(
    value: PropertyType, maxValue: PropertyType, minValue: PropertyType
) = RangeProperty(value, maxValue, minValue)