package com.jhworks.imageselect.crop.callback

import android.net.Uri

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/7/7 17:54
 */
interface BitmapCropCallback {
    fun onBitmapCropped(resultUri: Uri, offsetX: Int, offsetY: Int, imageWidth: Int, imageHeight: Int)

    fun onCropFailure(t: Throwable)
}