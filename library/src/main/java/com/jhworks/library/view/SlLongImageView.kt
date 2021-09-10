package com.jhworks.library.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import android.util.AttributeSet
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.ImageViewState
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.jhworks.library.utils.SlImageUtils
import com.jhworks.library.utils.SlLog
import java.io.File

/**
 * 长图预览，自动判断横向或者纵向展示
 * @author jackson
 * @version 1.0
 * @date 2021/9/10 10:09
 */
private const val HORIZONTAL = 0
private const val VERTICAL = 1

class SlLongImageView(context: Context, attrs: AttributeSet?) :
    SubsamplingScaleImageView(context, attrs) {
    constructor(context: Context) : this(context, null)


    fun setData(file: File?, resource: Bitmap?, url: String?) {
        SlLog.e("setData1: ${file}, $resource")
        if (file == null || resource == null) return
        SlLog.e("setData2: ${file}, $resource")
        val resourceW = resource.width.toFloat()
        val resourceH = resource.height.toFloat()
        val ratio = SlImageUtils.calculateImageRatio(context, resourceW, resourceH)
        val offsetW = ratio[0]
        val offsetH = ratio[1]
        if (offsetW > SlImageUtils.RATIO_W) {
            // 横向长图
            SlLog.i("[${url}]横向长图: ${resourceW}*${resourceH}")
            setLongImageView(file, HORIZONTAL)
        } else if (offsetH > SlImageUtils.RATIO_H) {
            //纵向长图
            SlLog.i("[${url}]纵向长图: ${resourceW}*${resourceH}")
            setLongImageView(file, VERTICAL)
        }
    }

    private fun setLongImageView(file: File, orientation: Int) {
        when (orientation) {
            VERTICAL -> {
                setMinimumScaleType(SCALE_TYPE_CENTER_CROP)
                setImage(
                    ImageSource.uri(file.absolutePath),
                    ImageViewState(0f, PointF(0f, 0f), ORIENTATION_0)
                )
            }
            HORIZONTAL -> {
                setImage(ImageSource.uri(file.absolutePath))
            }
        }
    }
}