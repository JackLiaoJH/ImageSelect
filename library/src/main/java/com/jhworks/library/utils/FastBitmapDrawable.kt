package com.jhworks.library.utils

import android.graphics.*
import android.graphics.drawable.Drawable

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/7/7 17:19
 */
class FastBitmapDrawable(private val bitmap: Bitmap?) : Drawable() {
    private val mPaint = Paint(Paint.FILTER_BITMAP_FLAG)

    private var mBitmap: Bitmap? = null
    private var mAlpha = 0
    private var mWidth = 0
    private var mHeight: Int = 0

    init {
        mAlpha = 255
        setBitmap(bitmap)
    }

    override fun draw(canvas: Canvas) {
        if (mBitmap != null && !mBitmap!!.isRecycled) {
            canvas.drawBitmap(mBitmap!!, null, bounds, mPaint)
        }
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
        return mBitmap
    }

    fun setBitmap(b: Bitmap?) {
        mBitmap = b
        if (b != null) {
            mWidth = mBitmap!!.width
            mHeight = mBitmap!!.height
        } else {
            mHeight = 0
            mWidth = mHeight
        }
    }
}