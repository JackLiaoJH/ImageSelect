package com.jhworks.library.utils

/**
 *
 * 空对象检查
 *
 * @author jiahui
 * date 2018/1/9
 */
object CheckNullUtils {

    fun check(o: Any?, msg: String = "$o can not null!!!") {
        if (o == null) throw RuntimeException(msg)
    }

    fun <T> isListEmpty(list: MutableList<T>?): Boolean {
        if (list == null || list.isEmpty()) return true
        return false
    }
}