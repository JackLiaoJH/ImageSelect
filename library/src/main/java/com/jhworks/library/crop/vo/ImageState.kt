package com.jhworks.library.crop.vo

import android.graphics.RectF

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/7/7 17:55
 */
data class ImageState(
        val cropRect: RectF,
        val currentImageRect: RectF,
        val currentScale: Float,
        var currentAngle: Float
)