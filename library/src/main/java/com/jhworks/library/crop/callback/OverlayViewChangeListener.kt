package com.jhworks.library.crop.callback

import android.graphics.RectF

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/7/7 19:06
 */
interface OverlayViewChangeListener {
    fun onCropRectUpdated(cropRect: RectF)
}