package com.jhworks.imageselect.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.jhworks.imageselect.R
import com.jhworks.imageselect.crop.vo.AspectRatio
import java.util.*

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/7/15 16:26
 */
class AspectRatioTextView(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
    : AppCompatTextView(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private val MARGIN_MULTIPLIER = 1.5f
    private val mCanvasClipBounds = Rect()
    private var mDotPaint: Paint
    private var mDotSize = 0
    private var mAspectRatio = 0f

    private var mAspectRatioTitle: String? = null
    private var mAspectRatioX = 0f
    private var mAspectRatioY = 0f

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.sl_AspectRatioTextView)
        gravity = Gravity.CENTER_HORIZONTAL

        mAspectRatioTitle = a.getString(R.styleable.sl_AspectRatioTextView_sl_artv_ratio_title)
        mAspectRatioX = a.getFloat(R.styleable.sl_AspectRatioTextView_sl_artv_ratio_x, CropImageView.SOURCE_IMAGE_ASPECT_RATIO)
        mAspectRatioY = a.getFloat(R.styleable.sl_AspectRatioTextView_sl_artv_ratio_y, CropImageView.SOURCE_IMAGE_ASPECT_RATIO)

        mAspectRatio = if (mAspectRatioX == CropImageView.SOURCE_IMAGE_ASPECT_RATIO || mAspectRatioY == CropImageView.SOURCE_IMAGE_ASPECT_RATIO) {
            CropImageView.SOURCE_IMAGE_ASPECT_RATIO
        } else {
            mAspectRatioX / mAspectRatioY
        }

        mDotSize = getContext().resources.getDimensionPixelSize(R.dimen.ucrop_size_dot_scale_text_view)
        mDotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mDotPaint.style = Paint.Style.FILL

        setTitle()

        applyActiveColor(Color.BLACK)
        a.recycle()
    }

    /**
     * @param activeColor the resolved color for active elements
     */
    fun setActiveColor(@ColorInt activeColor: Int) {
        applyActiveColor(activeColor)
        invalidate()
    }

    fun setAspectRatio(aspectRatio: AspectRatio) {
        mAspectRatioTitle = aspectRatio.aspectRatioTitle
        mAspectRatioX = aspectRatio.aspectRatioX
        mAspectRatioY = aspectRatio.aspectRatioY
        mAspectRatio = if (mAspectRatioX == CropImageView.SOURCE_IMAGE_ASPECT_RATIO
                || mAspectRatioY == CropImageView.SOURCE_IMAGE_ASPECT_RATIO) {
            CropImageView.SOURCE_IMAGE_ASPECT_RATIO
        } else {
            mAspectRatioX / mAspectRatioY
        }
        setTitle()
    }

    fun getAspectRatio(toggleRatio: Boolean): Float {
        if (toggleRatio) {
            toggleAspectRatio()
            setTitle()
        }
        return mAspectRatio
    }

    private fun setTitle() {
        text = if (!TextUtils.isEmpty(mAspectRatioTitle)) {
            mAspectRatioTitle
        } else {
            String.format(Locale.US, "%d:%d", mAspectRatioX.toInt(), mAspectRatioY.toInt())
        }
    }

    private fun applyActiveColor(@ColorInt activeColor: Int) {
        mDotPaint.color = activeColor
        val textViewColorStateList = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf(0)),
                intArrayOf(activeColor, ContextCompat.getColor(context, R.color.ucrop_color_widget))
        )
        setTextColor(textViewColorStateList)
    }

    private fun toggleAspectRatio() {
        if (mAspectRatio != CropImageView.SOURCE_IMAGE_ASPECT_RATIO) {
            val tempRatioW = mAspectRatioX
            mAspectRatioX = mAspectRatioY
            mAspectRatioY = tempRatioW
            mAspectRatio = mAspectRatioX / mAspectRatioY
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isSelected) {
            canvas.getClipBounds(mCanvasClipBounds)
            val x = (mCanvasClipBounds.right - mCanvasClipBounds.left) / 2.0f
            val y = mCanvasClipBounds.bottom - mCanvasClipBounds.top / 2f - mDotSize * MARGIN_MULTIPLIER
            canvas.drawCircle(x, y, mDotSize / 2f, mDotPaint)
        }
    }
}