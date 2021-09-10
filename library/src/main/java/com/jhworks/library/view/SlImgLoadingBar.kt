package com.jhworks.library.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

/**
 * Image loading progress bar
 * @author jackson
 * @version 1.0
 * @date 2021/9/9 18:02
 */
@SuppressLint("ViewConstructor")
class SlImgLoadingBar(context: Context, private var circleRadius: Int = 70) : View(context) {
    private var maxValue: Long = 100
    private var progress: Long = 0
    private var circlePaint = Paint()
    private var circleWidth = 0f
    private var circleProgressColor = 0
    private var circleBottomColor = 0
    private var circleBottomWidth = 0

    //两个圆之间的间隔
    private var interval = 0

    init {
        circlePaint.isAntiAlias = true
        circlePaint.strokeWidth = 10f
        circlePaint.strokeCap = Paint.Cap.ROUND
        circleProgressColor = -0x22222223
        circleBottomColor = -0x22222223
        circleWidth = 8f
        interval = 5
        circleBottomWidth = 2
        progress = 1
    }


    /*
     * 画空心圆
     */
    private fun drawCircle(canvas: Canvas) {
        val xPos = left + (width shr 1)
        val yPos = top + (height shr 1)
        circlePaint.color = circleBottomColor
        circlePaint.strokeWidth = circleBottomWidth.toFloat()
        circlePaint.style = Paint.Style.STROKE
        circlePaint.shader = null
        canvas.drawCircle(
            xPos.toFloat(),
            yPos.toFloat(),
            (circleRadius + interval).toFloat(),
            circlePaint
        )
    }

    /*
     * 根据进度条画实心圆
     */
    private fun drawArc(canvas: Canvas) {
        circlePaint.style = Paint.Style.FILL
        val xPos = left + (width shr 1)
        val yPos = top + (height shr 1)
        val rectF = RectF(
            (xPos - circleRadius).toFloat(),
            (yPos - circleRadius).toFloat(),
            (xPos + circleRadius).toFloat(),
            (yPos + circleRadius).toFloat()
        )
        val degree = progress.toFloat() / maxValue.toFloat() * 360
        circlePaint.strokeWidth = circleWidth
        circlePaint.color = circleProgressColor
        canvas.drawArc(rectF, -90f, degree, true, circlePaint)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if ((progress.toFloat() / maxValue.toFloat() * 100).toInt() == 100) return

        drawCircle(canvas)
        drawArc(canvas)
    }

    fun setProgress(mProgress: Long) {
        val origin = progress
        progress = mProgress
        if (progress != 0L && origin != progress) {
            postInvalidate()
        }
    }

    fun setMaxValue(value: Long): SlImgLoadingBar {
        maxValue = value
        return this
    }

    fun setCircleWidth(circleWidth: Float): SlImgLoadingBar {
        this.circleWidth = circleWidth
        return this
    }

    fun setCircleBottomWidth(circleBottomWidth: Int): SlImgLoadingBar {
        this.circleBottomWidth = circleBottomWidth
        return this
    }

    fun setCircleRadius(circleRadius: Int): SlImgLoadingBar {
        this.circleRadius = circleRadius
        return this
    }

    fun setInterval(interval: Int): SlImgLoadingBar {
        this.interval = interval
        return this
    }
}