package com.jhworks.imageselect.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.jhworks.imageselect.R

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/7/15 14:43
 */
class HorizontalProgressWheelView(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
    : View(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private val mCanvasClipBounds = Rect()

    private var mScrollingListener: ScrollingListener? = null
    private var mLastTouchedPosition = 0f

    private var mProgressLinePaint: Paint
    private var mProgressMiddleLinePaint: Paint
    private var mProgressLineWidth = 0
    private var mProgressLineHeight = 0
    private var mProgressLineMargin = 0

    private var mScrollStarted = false
    private var mTotalScrollDistance = 0f

    private var mMiddleLineColor = 0


    init {
        mMiddleLineColor = ContextCompat.getColor(context, R.color.ucrop_color_widget_rotate_mid_line)

        mProgressLineWidth = getContext().resources.getDimensionPixelSize(R.dimen.ucrop_width_horizontal_wheel_progress_line)
        mProgressLineHeight = getContext().resources.getDimensionPixelSize(R.dimen.ucrop_height_horizontal_wheel_progress_line)
        mProgressLineMargin = getContext().resources.getDimensionPixelSize(R.dimen.ucrop_margin_horizontal_wheel_progress_line)

        mProgressLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mProgressLinePaint.style = Paint.Style.STROKE
        mProgressLinePaint.strokeWidth = mProgressLineWidth.toFloat()
        mProgressLinePaint.color = resources.getColor(R.color.ucrop_color_progress_wheel_line)

        mProgressMiddleLinePaint = Paint(mProgressLinePaint)
        mProgressMiddleLinePaint.color = mMiddleLineColor
        mProgressMiddleLinePaint.strokeCap = Paint.Cap.ROUND
        mProgressMiddleLinePaint.strokeWidth = resources.getDimensionPixelSize(R.dimen.ucrop_width_middle_wheel_progress_line).toFloat()
    }


    fun setScrollingListener(scrollingListener: ScrollingListener) {
        mScrollingListener = scrollingListener
    }

    fun setMiddleLineColor(@ColorInt middleLineColor: Int) {
        mMiddleLineColor = middleLineColor
        mProgressMiddleLinePaint.color = mMiddleLineColor
        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> mLastTouchedPosition = event.x
            MotionEvent.ACTION_UP -> {
                mScrollStarted = false
                mScrollingListener?.onScrollEnd()
            }
            MotionEvent.ACTION_MOVE -> {
                val distance = event.x - mLastTouchedPosition
                if (distance != 0f) {
                    if (!mScrollStarted) {
                        mScrollStarted = true

                        mScrollingListener?.onScrollStart()
                    }
                    onScrollEvent(event, distance)
                }
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.getClipBounds(mCanvasClipBounds)
        val linesCount = mCanvasClipBounds.width() / (mProgressLineWidth + mProgressLineMargin)
        val deltaX = mTotalScrollDistance % (mProgressLineMargin + mProgressLineWidth).toFloat()
        for (i in 0 until linesCount) {
            when {
                i < linesCount / 4 -> {
                    mProgressLinePaint.alpha = (255 * (i / (linesCount / 4).toFloat())).toInt()
                }
                i > linesCount * 3 / 4 -> {
                    mProgressLinePaint.alpha = (255 * ((linesCount - i) / (linesCount / 4).toFloat())).toInt()
                }
                else -> {
                    mProgressLinePaint.alpha = 255
                }
            }
            canvas.drawLine(
                    -deltaX + mCanvasClipBounds.left + i * (mProgressLineWidth + mProgressLineMargin),
                    mCanvasClipBounds.centerY() - mProgressLineHeight / 4.0f,
                    -deltaX + mCanvasClipBounds.left + i * (mProgressLineWidth + mProgressLineMargin),
                    mCanvasClipBounds.centerY() + mProgressLineHeight / 4.0f, mProgressLinePaint)
        }
        canvas.drawLine(
                mCanvasClipBounds.centerX().toFloat(),
                mCanvasClipBounds.centerY() - mProgressLineHeight / 2.0f,
                mCanvasClipBounds.centerX().toFloat(),
                mCanvasClipBounds.centerY() + mProgressLineHeight / 2.0f, mProgressMiddleLinePaint
        )
    }

    private fun onScrollEvent(event: MotionEvent, distance: Float) {
        mTotalScrollDistance -= distance
        postInvalidate()
        mLastTouchedPosition = event.x
        mScrollingListener?.onScroll(-distance, mTotalScrollDistance)
    }

    interface ScrollingListener {
        fun onScrollStart()
        fun onScroll(delta: Float, totalDistance: Float)
        fun onScrollEnd()
    }
}