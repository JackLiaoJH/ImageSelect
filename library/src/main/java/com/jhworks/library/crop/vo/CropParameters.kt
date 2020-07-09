package com.jhworks.library.crop.vo

import android.graphics.Bitmap.CompressFormat

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/7/7 17:57
 */
class CropParameters(
        val maxResultImageSizeX: Int,
        var maxResultImageSizeY: Int,
        val compressFormat: CompressFormat,
        val compressQuality: Int,
        val imageInputPath: String?,
        var imageOutputPath: String?,
        val exifInfo: ExifInfo?
) {


}