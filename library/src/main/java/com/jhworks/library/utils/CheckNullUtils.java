package com.jhworks.library.utils;

/**
 * <p>空对象检查 </p>
 *
 * @author jiahui
 *         date 2018/1/9
 */
public class CheckNullUtils {

    public static void check(Object o) {
        if (o == null) throw new RuntimeException(o + " can not null!!!");
    }
}
