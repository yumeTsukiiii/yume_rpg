package fan.yumetsuki.yumerpg.serialization

/**
 * 字符串生成 long hashCode 的方法，源于 IOS 中的字符串 hash（据说比 Java 的 .hashCode()冲突率要低）
 * 见源码链接 https://www.cnblogs.com/dins/p/ios-hash.html
 * @author yumetsuki
 */
fun String.longHashCode(): Long {
    if (length > 96) {
        error("字符串长度必须小于96")
    }

    val len = length
    var result = len.toLong()
    val end4 = len and -4
    var i = 0
    while (i < end4) {
        result =
            result * 67503105L + (get(i).code * 16974593L) + (get(i + 1).code * 66049L) + (get(i + 2).code * 257L) + get(i + 3).code.toLong()
        i += 4
    }
    while (i < len) {
        result = result * 257L + get(i).code.toLong()
        ++i
    }
    return result + (result shl (len and 31))
}