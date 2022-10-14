package fan.yumetsuki.yumerpg.utils

/**
 * 当[value]为[Value]泛型实例，向[MutableMap]中放入该实例
 * @param key Map 中的 key 值
 * @param value Map 中的 value 值
 * @author yumetsuki
 */
inline fun <reified Value, Key> MutableMap<Key, Value>.putIfIsInstance(key: Key, value: Any?) {
    if (value is Value) {
        put(key, value)
    }
}