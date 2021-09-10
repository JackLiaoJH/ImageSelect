package com.jhworks.library.utils

import android.util.Log

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2021/9/10 23:14
 */
object SlLog {
    var isDebug = false
    private const val TAG = "ImageSelector>>>>>"

    fun e(msg: String) {
        if (!isDebug) return
        Log.e(TAG, msg)
    }

    fun d(msg: String) {
        if (!isDebug) return
        Log.d(TAG, msg)
    }

    fun i(msg: String) {
        if (!isDebug) return
        Log.i(TAG, msg)
    }

    fun w(msg: String) {
        if (!isDebug) return
        Log.w(TAG, msg)
    }
}