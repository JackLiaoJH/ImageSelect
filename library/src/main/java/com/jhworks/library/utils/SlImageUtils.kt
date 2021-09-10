package com.jhworks.library.utils

import android.content.Context
import android.graphics.Bitmap

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2021/9/10 00:07
 */
object SlImageUtils {

    //大于此值即表示横向长图
    const val RATIO_W = 1.05f

    //大于此值即表示纵向长图
    const val RATIO_H = 0.8f


    fun isGifOrWebp(url: String?): Boolean {
        url ?: return false
        val fileName = url.substring(url.lastIndexOf("/") + 1)
        return fileName.endsWith(".webp") || fileName.endsWith(".gif")
    }


    fun isLongImage(context: Context, resource: Bitmap?, url: String?): Boolean {
        if (isGifOrWebp(url) || resource == null) return false
        val ratio =
            calculateImageRatio(context, resource.width.toFloat(), resource.height.toFloat())
        return ratio[0] > RATIO_W || ratio[1] > RATIO_H
    }

    fun calculateImageRatio(context: Context, imageWidth: Float, imageHeight: Float): FloatArray {
        val screenWith = SlScreenUtils.getScreenWidth(context)
        val screenHeight = SlScreenUtils.getScreenHeight(context)
        val offsetW = imageWidth / imageHeight - screenWith / screenHeight
        val offsetH = imageHeight / imageWidth - screenHeight / screenWith
        return floatArrayOf(offsetW, offsetH)
    }
}