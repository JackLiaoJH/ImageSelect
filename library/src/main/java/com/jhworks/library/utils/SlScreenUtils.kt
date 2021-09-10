package com.jhworks.library.utils

import android.content.Context
import android.graphics.Point
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

/**
 * 屏幕工具
 */
object SlScreenUtils {

    private var screenWidth = 0
    private var screenHeight = 0

    fun getScreenSize(context: Context): Point {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val out = Point()
        wm.defaultDisplay.getSize(out)
        return out
    }

    fun getScreenWidth(context: Context): Int {
        if (screenWidth > 0) return screenWidth
        val metric = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metric)
        screenWidth = metric.widthPixels
        return screenWidth
    }

    fun getScreenHeight(context: Context): Int {
        if (screenHeight > 0) return screenHeight
        val metric = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metric)
        screenHeight = metric.heightPixels
        return screenHeight
    }


    fun getColor(context: Context, @ColorRes colorRes: Int): Int {
        return ContextCompat.getColor(context, colorRes)
    }
}