package com.jhworks.library.crop.callback

import android.graphics.Bitmap
import com.jhworks.library.crop.vo.ExifInfo

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/7/7 17:33
 */
interface BitmapLoadCallback {
    fun onBitmapLoaded(bitmap: Bitmap, exifInfo: ExifInfo, imageInputPath: String, imageOutputPath: String)

    fun onFailure(bitmapWorkerException: Exception)
}