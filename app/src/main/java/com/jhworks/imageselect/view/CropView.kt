package com.jhworks.imageselect.view

import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout
import com.jhworks.imageselect.R
import com.jhworks.imageselect.crop.callback.CropBoundsChangeListener
import com.jhworks.imageselect.crop.callback.OverlayViewChangeListener

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/7/7 16:51
 */
class CropView(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
    : FrameLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var mGestureCropImageView: GestureCropImageView
    private var mViewOverlay: OverlayView

    init {
        inflate(context, R.layout.sl_crop_view, this)

        mGestureCropImageView = findViewById(R.id.image_view_crop)
        mViewOverlay = findViewById(R.id.view_overlay)

        val a = context.obtainStyledAttributes(attrs, R.styleable.sl_CropView)
        mViewOverlay.processStyledAttributes(a)
        mGestureCropImageView.processStyledAttributes(a)
        a.recycle()


        setListenersToViews()
    }

    private fun setListenersToViews() {
        mGestureCropImageView.setCropBoundsChangeListener(object : CropBoundsChangeListener {
            override fun onCropAspectRatioChanged(cropRatio: Float) {
                mViewOverlay.setTargetAspectRatio(cropRatio)
            }
        })
        mViewOverlay.setOverlayViewChangeListener(object : OverlayViewChangeListener {
            override fun onCropRectUpdated(cropRect: RectF) {
                mGestureCropImageView.setCropRect(cropRect)
            }
        })
    }

    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }

    fun getCropImageView(): GestureCropImageView {
        return mGestureCropImageView
    }

    fun getOverlayView(): OverlayView {
        return mViewOverlay
    }

    /**
     * Method for reset state for UCropImageView such as rotation, scale, translation.
     * Be careful: this method recreate UCropImageView instance and reattach it to layout.
     */
    fun resetCropImageView() {
        removeView(mGestureCropImageView)
        mGestureCropImageView = GestureCropImageView(context)
        setListenersToViews()
        mGestureCropImageView.setCropRect(getOverlayView().getCropViewRect())
        addView(mGestureCropImageView, 0)
    }
}