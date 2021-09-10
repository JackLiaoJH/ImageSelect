package com.jhworks.library.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.jhworks.library.core.ui.OnItemClickListener
import com.jhworks.library.utils.SlScreenUtils
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

//val TAG = "ScrollDownPhotoView"

private const val TOUCH_MODE_NONE = 0
private const val TOUCH_MODE_POINTER = 1
private const val TOUCH_MODE_POINTER_CHILD = 2
private const val MIN_SCALE_WEIGHT = 0.25f
private const val DURATION = 200L
private const val DRAG_GAP_PX = 50.0f

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2021/9/9 17:10
 */
class ScrollDownPhotoView : FrameLayout {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}


    private var touchMode = TOUCH_MODE_NONE
    private var pointerDownX = 0f
    private var pointerDownY = 0f
    private var downX = 0f
    private var downY = 0f
    private var screenHeight = 0
    private var finishDeltaY = 0f

    private var isDragging = false
    private var isScaleFinish = true

    //是否正在归位
    private var isFling = false
    private var viewPager: View? = null

    /**
     * 是否开启下滑关闭activity，默认开启。类似微信的图片浏览，可下滑关闭一样，但是没有图片归位效果
     */
    private var isOpenDownAnimate = true

    init {
        screenHeight = SlScreenUtils.getScreenHeight(context)
        setBackgroundColor(Color.BLACK)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewPager = getChildAt(0)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        ev ?: return super.onTouchEvent(ev)
        if (!isOpenDownAnimate || isFling) {
            return false
        }
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.rawX
                downY = ev.rawY
                isDragging = false
                touchMode = TOUCH_MODE_NONE
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX: Float = ev.rawX - downX
                val deltaY: Float = ev.rawY - downY
                if (touchMode != TOUCH_MODE_POINTER && (isDragging || deltaY > DRAG_GAP_PX)) {
                    onViewTouchListener?.onMoving(deltaX, deltaY)
                    if (downX == 0f || deltaY == 0f) {
                        //这里是防止手指多次快速按下移动导致图片错位的方案
                        downX = ev.rawX
                        downY = ev.rawY
                    }
                    isDragging = true
                    onDrag(ev.rawX, ev.rawY)
                    return true
                }
            }
            MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_POINTER_UP ->
                onTouchActivePointer(ev)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val upX: Float = ev.rawX
                val upY: Float = ev.rawY
                if (touchMode == TOUCH_MODE_NONE) {
                    if (upY > downY && abs(upY - downY) > screenHeight shr 3) {
                        if (onViewTouchListener != null) {
                            finishActivity()
                        } else {
                            onFling(upX, upY)
                        }
                    } else {
                        onFling(upX, upY)
                    }
                } else if (touchMode == TOUCH_MODE_POINTER) { //这里是双指操作，不作关闭activity判断，只是图片归位
                    onFling(pointerDownX, pointerDownY)
                } else {
                    onFling(upX, upY)
                }
                isDragging = false
            }
        }
        return super.onTouchEvent(ev)
    }


    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        ev ?: return super.onInterceptTouchEvent(ev)
        if (!isOpenDownAnimate || isFling) {
            return false
        }
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.rawX
                downY = ev.rawY
                isDragging = false
                touchMode = TOUCH_MODE_NONE
                pointerDownX = 0f
                pointerDownY = 0f
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaY = ev.rawY - downY
                if (isScaleFinish && touchMode != TOUCH_MODE_POINTER_CHILD && deltaY > DRAG_GAP_PX) {
                    //不是PhotoView的多指操作，是下拉，进行拦截
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
            }
        }
        return false
    }


    private fun onTouchActivePointer(ev: MotionEvent) {
        if (pointerDownX == 0f || pointerDownY == 0f) {
            pointerDownX = ev.rawX
            pointerDownY = ev.rawY
        }
        touchMode = TOUCH_MODE_POINTER
    }

    /**
     * 图片归位,移动到原来位置
     */
    private fun onFling(upX: Float, upY: Float) {
        if (upY != downY) {
            val valueAnimator = ValueAnimator.ofFloat(upY, downY)
            valueAnimator.duration = DURATION
            valueAnimator.addUpdateListener { animation ->
                val y = animation.animatedValue as Float
                val percent = (y - downY) / (upY - downY)
                val x = percent * (upX - downX) + downX
                onDrag(x, y)
                if (y == downY) {
                    downY = 0f
                    downX = 0f
                }
            }
            valueAnimator.addListener(flingAnimatorListenerAdapter)
            valueAnimator.start()
        } else if (upX != downX) {
            val valueAnimator = ValueAnimator.ofFloat(upX, downX)
            valueAnimator.duration = DURATION
            valueAnimator.addUpdateListener { animation ->
                val x = animation.animatedValue as Float
                val percent = (x - downX) / (upX - downX)
                val y = percent * (upY - downY) + downY
                onDrag(x, y)
                if (x == downX) {
                    downY = 0f
                    downX = 0f
                }
            }
            valueAnimator.addListener(flingAnimatorListenerAdapter)
            valueAnimator.start()
        }
    }

    private val flingAnimatorListenerAdapter: AnimatorListenerAdapter =
        object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator) {
                isFling = false
            }

            override fun onAnimationStart(animation: Animator) {
                isFling = true
            }

            override fun onAnimationEnd(animation: Animator) {
                isFling = false
            }
        }

    private fun onDrag(dx: Float, dy: Float) {
        val deltaX = dx - downX
        val deltaY = dy - downY
        var scale = 1f
        if (deltaY > 0) {
            scale = 1 - abs(deltaY) / screenHeight
        }
        //移动
        viewPager?.translationX = deltaX
        viewPager?.translationY = deltaY
        //缩放
        scale = min(max(scale, MIN_SCALE_WEIGHT), 1f)
        viewPager?.scaleX = scale
        viewPager?.scaleY = scale
        //设置背景颜色
        val alpha = scale * 255
        setBackgroundColor(Color.argb(alpha.toInt(), 0, 0, 0))
        finishDeltaY = deltaY
    }

    private fun finishActivity() {
        val valueAnimator = ValueAnimator.ofFloat(finishDeltaY, screenHeight.toFloat())
        valueAnimator.duration = 350L
        valueAnimator.addUpdateListener { animation ->
            val y = animation.animatedValue as Float
            viewPager?.translationY = y
            val scale =
                min(max(1 - abs(y) / screenHeight, 0f), 1f)
            val alpha = scale * 255
            setBackgroundColor(Color.argb(alpha.toInt(), 0, 0, 0))
        }
        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                setBackgroundColor(Color.argb(0, 0, 0, 0))
                onViewTouchListener?.onFinish()
            }
        })
        onViewTouchListener?.onPreFinish()
        valueAnimator.start()
    }

    fun setOpenDownAnimate(openDownAnimate: Boolean) {
        isOpenDownAnimate = openDownAnimate
    }


    fun setScaleFinish(scaleFinish: Boolean) {
        isScaleFinish = scaleFinish
    }

    interface OnViewTouchListener {
        fun onFinish()
        fun onPreFinish()
        fun onMoving(deltaX: Float, deltaY: Float)
    }

    private var onViewTouchListener: OnViewTouchListener? = null

    fun setOnViewTouchListener(onViewTouchListener: OnViewTouchListener) {
        this.onViewTouchListener = onViewTouchListener
    }
}