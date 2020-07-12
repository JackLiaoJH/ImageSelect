package com.jhworks.imageselect.utils

import android.graphics.*
import android.graphics.drawable.Drawable

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/7/7 17:19
 */
class FastBitmapDrawable(private var bitmap: Bitmap?) : Drawable() {
    private val mPaint = Paint(Paint.FILTER_BITMAP_FLAG)

    private var mAlpha = 0
    private var mWidth = 0
    private var mHeight = 0

    init {
        mAlpha = 255
        setBitmap(bitmap)
    }

    override fun draw(canvas: Canvas) {
        bitmap?.let { if (!it.isRecycled) canvas.drawBitmap(it, null, bounds, mPaint) }
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mPaint.colorFilter = cf
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setFilterBitmap(filterBitmap: Boolean) {
        mPaint.isFilterBitmap = filterBitmap
    }

    override fun getAlpha(): Int {
        return mAlpha
    }

    override fun setAlpha(alpha: Int) {
        mAlpha = alpha
        mPaint.alpha = alpha
    }

    override fun getIntrinsicWidth(): Int {
        return mWidth
    }

    override fun getIntrinsicHeight(): Int {
        return mHeight
    }

    override fun getMinimumWidth(): Int {
        return mWidth
    }

    override fun getMinimumHeight(): Int {
        return mHeight
    }

    fun getBitmap(): Bitmap? {
        return bitmap
    }

    fun setBitmap(b: Bitmap?) {
        bitmap = b
        if (b != null) {
            mWidth = b.width
            mHeight = b.height
        } else {
            mHeight = 0
            mWidth = mHeight
        }
    }
}