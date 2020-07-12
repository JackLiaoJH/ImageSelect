package com.jhworks.imageselect.crop.vo

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/7/7 17:27
 */
data class ExifInfo(
        val exifOrientation: Int = 0,
        val exifDegrees: Int = 0,
        val exifTranslation: Int = 0
) {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val (exifOrientation1, exifDegrees1, exifTranslation1) = o as ExifInfo

        if (exifOrientation != exifOrientation1) return false
        return if (exifDegrees != exifDegrees1) false else exifTranslation == exifTranslation1
    }

    override fun hashCode(): Int {
        var result: Int = exifOrientation
        result = 31 * result + exifDegrees
        result = 31 * result + exifTranslation
        return result
    }
}