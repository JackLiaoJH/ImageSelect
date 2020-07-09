package com.jhworks.library.utils

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/7/7 17:48
 */
object CubicEasing {

    fun easeOut(time: Float, start: Float, end: Float, duration: Float): Float {
        var time = time
        return end * ((time / duration - 1.0f.also { time = it }) * time * time + 1.0f) + start
    }

    fun easeIn(time: Float, start: Float, end: Float, duration: Float): Float {
        var time = time
        return end * duration.let { time /= it; time } * time * time + start
    }

    fun easeInOut(time: Float, start: Float, end: Float, duration: Float): Float {
        var time = time
        return if (duration / 2.0f.let { time /= it; time } < 1.0f) end / 2.0f * time * time * time + start else end / 2.0f * (2.0f.let { time -= it; time } * time * time + 2.0f) + start
    }

}