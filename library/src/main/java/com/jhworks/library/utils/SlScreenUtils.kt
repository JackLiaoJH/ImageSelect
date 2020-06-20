package com.jhworks.library.utils

import android.content.Context
import android.graphics.Point
import android.view.WindowManager

/**
 * 屏幕工具
 */
object SlScreenUtils {

    fun getScreenSize(context: Context): Point {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val out = Point()
        wm.defaultDisplay.getSize(out)
        return out
    }
}