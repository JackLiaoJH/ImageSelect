package com.jhworks.imageselect.crop

import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.jhworks.imageselect.R
import com.jhworks.imageselect.crop.callback.BitmapCropCallback
import com.jhworks.imageselect.crop.vo.AspectRatio
import com.jhworks.imageselect.view.*
import com.jhworks.imageselect.view.CropImageView.Companion.SOURCE_IMAGE_ASPECT_RATIO
import com.jhworks.library.core.MediaConstant
import com.jhworks.library.core.ui.ImageBaseActivity
import java.io.File
import java.util.*

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/7/7 14:19
 */
class ImageCropActivity : ImageBaseActivity(), TransformImageView.TransformImageListener, View.OnClickListener {

    companion object {
        private const val CONTROLS_ANIMATION_DURATION: Long = 50
        private const val TABS_COUNT = 3
        private const val SCALE_WIDGET_SENSITIVITY_COEFFICIENT = 15000
        private const val ROTATE_WIDGET_SENSITIVITY_COEFFICIENT = 42

        const val NONE = 0
        const val SCALE = 1
        const val ROTATE = 2
        const val ALL = 3
    }

    private lateinit var mGestureCropImageView: GestureCropImageView
    private lateinit var mOverlayView: OverlayView
    private lateinit var mCropView: CropView

    // control
    private lateinit var mTvStatusScale: TextView
    private lateinit var mTvStatusRotate: TextView
    private lateinit var mTvStatusRatio: TextView
    private lateinit var mProgressWheelView: HorizontalProgressWheelView
    private lateinit var mTvValue: TextView
    private lateinit var mLlAspectRatio: LinearLayout
    private lateinit var mIvReset: ImageView
    private lateinit var mIvAngle: ImageView
    private var mCurrentStatusId: Int = R.id.tv_status_ratio

    private var mBlockingView: View? = null

    private var mShowLoader = true
    private val mCompressFormat = MediaConstant.DEFAULT_COMPRESS_FORMAT
    private val mCompressQuality = MediaConstant.DEFAULT_COMPRESS_QUALITY

    @DrawableRes
    private val mToolbarCropDrawable = R.drawable.ic_sl_crop_done
    private var mToolbarWidgetColor = 0

    private val mCropAspectRatioViews = arrayListOf<AspectRatioTextView>()

    override fun setLayout(): Int = R.layout.activity_sl_image_crop

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent.getParcelableExtra<Uri>("uri")

        mToolbarWidgetColor = Color.WHITE



        initView()

        mGestureCropImageView.setRotateEnabled(false)
        mGestureCropImageView.setTransformImageListener(this)


        uri?.let {
            val outUri = Uri.fromFile(File(cacheDir, "crop.jpg"))
            mGestureCropImageView.setImageUri(it, outUri)
            Log.e("crop", "out:$outUri , in:$it")
        }

        addBlockingView()
        showWheel(false)
        updateCropControlUi(R.id.tv_status_ratio)

        val aspectRatioList = arrayListOf(
                AspectRatio(null, 1f, 1f),
                AspectRatio(null, 3f, 4f),
                AspectRatio(getString(R.string.sl_label_original), SOURCE_IMAGE_ASPECT_RATIO, SOURCE_IMAGE_ASPECT_RATIO),
                AspectRatio(null, 3f, 2f),
                AspectRatio(null, 16f, 9f)
        )
        val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
        lp.weight = 1f
        var aspectRatioTextView: AspectRatioTextView
        aspectRatioList.forEach {
            aspectRatioTextView = AspectRatioTextView(this)
            aspectRatioTextView.layoutParams = lp
            aspectRatioTextView.setAspectRatio(it)

            mLlAspectRatio.addView(aspectRatioTextView)
            mCropAspectRatioViews.add(aspectRatioTextView)
        }
        mCropAspectRatioViews[2].isSelected = true

        mCropAspectRatioViews.forEach { aspectView ->
            aspectView.setOnClickListener {
                mGestureCropImageView.setTargetAspectRatio(aspectView.getAspectRatio(it.isSelected))
                mGestureCropImageView.setImageToWrapCropBounds()
                if (!it.isSelected) {
                    mCropAspectRatioViews.forEach { temp ->
                        temp.isSelected = temp == it
                    }
                }
            }
        }
    }

    private fun initView() {
        mCropView = findViewById(R.id.sl_crop_view)
        mGestureCropImageView = mCropView.getCropImageView()
        mOverlayView = mCropView.getOverlayView()

        // control
        mTvStatusRatio = findViewById(R.id.tv_status_ratio)
        mTvStatusRatio.setOnClickListener(this)
        mTvStatusRotate = findViewById(R.id.tv_status_rotate)
        mTvStatusRotate.setOnClickListener(this)
        mTvStatusScale = findViewById(R.id.tv_status_scale)
        mTvStatusScale.setOnClickListener(this)
        mIvReset = findViewById(R.id.iv_sl_reset)
        mIvReset.setOnClickListener(this)
        mIvAngle = findViewById(R.id.iv_sl_angle)
        mIvAngle.setOnClickListener(this)
        mTvValue = findViewById(R.id.tv_sl_control_rotate)
        mLlAspectRatio = findViewById(R.id.layout_aspect_ratio)
        mProgressWheelView = findViewById(R.id.rotate_scroll_wheel)
        mProgressWheelView.setScrollingListener(object : HorizontalProgressWheelView.ScrollingListener {
            override fun onScrollStart() {
                detailCropControl({

                }, {
                    mGestureCropImageView.cancelAllAnimations()
                }, {
                    mGestureCropImageView.cancelAllAnimations()
                })
            }

            override fun onScroll(delta: Float, totalDistance: Float) {
                detailCropControl({

                }, {
                    mGestureCropImageView.postRotate(delta / ROTATE_WIDGET_SENSITIVITY_COEFFICIENT)
                }, {
                    if (delta > 0) {
                        mGestureCropImageView.zoomInImage(mGestureCropImageView.getCurrentScale()
                                + delta * ((mGestureCropImageView.getMaxScale() - mGestureCropImageView.getMinScale()) / SCALE_WIDGET_SENSITIVITY_COEFFICIENT))
                    } else {
                        mGestureCropImageView.zoomOutImage(mGestureCropImageView.getCurrentScale()
                                + delta * ((mGestureCropImageView.getMaxScale() - mGestureCropImageView.getMinScale()) / SCALE_WIDGET_SENSITIVITY_COEFFICIENT))
                    }
                })
            }

            override fun onScrollEnd() {
                detailCropControl({

                }, {
                    mGestureCropImageView.setImageToWrapCropBounds()
                }, {
                    mGestureCropImageView.setImageToWrapCropBounds()
                })
            }

        })
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.sl_crop_menu, menu)

        val menuItemCrop = menu.findItem(R.id.menu_crop)
        val menuItemCropIcon = ContextCompat.getDrawable(this, mToolbarCropDrawable)
        if (menuItemCropIcon != null) {
            menuItemCropIcon.mutate()
            menuItemCropIcon.setColorFilter(mToolbarWidgetColor, PorterDuff.Mode.SRC_ATOP)
            menuItemCrop.icon = menuItemCropIcon
        }

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.menu_crop).isVisible = true
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_crop -> {
                cropAndSaveImage()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun cropAndSaveImage() {
        mBlockingView?.isClickable = true
        mShowLoader = true
        supportInvalidateOptionsMenu()

        mGestureCropImageView.cropAndSaveImage(mCompressFormat, mCompressQuality, object : BitmapCropCallback {
            override fun onBitmapCropped(resultUri: Uri, offsetX: Int, offsetY: Int, imageWidth: Int, imageHeight: Int) {
//                setResultUri(resultUri, mGestureCropImageView.getTargetAspectRatio(), offsetX, offsetY, imageWidth, imageHeight)
                Log.e("xxxxxxxx", "压缩: $resultUri $imageWidth $imageHeight")
                ResultActivity.start(this@ImageCropActivity, resultUri)
                finish()
            }

            override fun onCropFailure(t: Throwable) {
                setResultError(t)
                finish()
            }
        })
    }

    override fun onLoadComplete() {
        mCropView.animate().alpha(1f).setDuration(300).interpolator = AccelerateInterpolator()
        mBlockingView?.isClickable = false
        mShowLoader = false
        supportInvalidateOptionsMenu()
    }

    override fun onLoadFailure(e: Exception) {
        setResultError(e)
        finish()
    }

    override fun onRotate(currentAngle: Float) {
        detailCropControl({}, {
            mTvValue.text = String.format(Locale.getDefault(), "%.1f°", currentAngle)
        }, {})
    }

    override fun onScale(currentScale: Float) {
        detailCropControl({}, {}, {
            mTvValue.text = String.format(Locale.getDefault(), "%d%%", (currentScale * 100).toInt())
        })
    }

    protected fun setResultError(throwable: Throwable?) {
        throwable?.printStackTrace()
        setResult(MediaConstant.RESULT_ERROR)
    }

    /**
     * Adds view that covers everything below the Toolbar.
     * When it's clickable - user won't be able to click/touch anything below the Toolbar.
     * Need to block user input while loading and cropping an image.
     */
    private fun addBlockingView() {
        if (mBlockingView == null) {
            mBlockingView = View(this)
            val lp = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            lp.addRule(RelativeLayout.BELOW, R.id.sl_toolbar)
            mBlockingView?.layoutParams = lp
            mBlockingView?.isClickable = true
        }
        findViewById<FrameLayout>(R.id.sl_crop_root).addView(mBlockingView)
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.tv_status_ratio -> {
                updateCropControlUi(v.id)
                showWheel(false)
            }
            R.id.tv_status_rotate -> {
                updateCropControlUi(v.id)
                showWheel(true)
            }
            R.id.tv_status_scale -> {
                updateCropControlUi(v.id)
                showWheel(true)
            }
            R.id.iv_sl_reset -> {
                resetRotation()
            }
            R.id.iv_sl_angle -> {
                rotateByAngle(90)
            }
        }
    }

    private fun rotateByAngle(angle: Int) {
        mGestureCropImageView.postRotate(angle.toFloat())
        mGestureCropImageView.setImageToWrapCropBounds()
    }

    private fun resetRotation() {
        mGestureCropImageView.postRotate(-mGestureCropImageView.getCurrentAngle())
        mGestureCropImageView.setImageToWrapCropBounds()
    }

    private inline fun detailCropControl(ratio: () -> Unit, rotate: () -> Unit, scale: () -> Unit) {
        when (mCurrentStatusId) {
            R.id.tv_status_ratio -> ratio.invoke()
            R.id.tv_status_rotate -> rotate.invoke()
            R.id.tv_status_scale -> scale.invoke()
        }
    }

    private fun updateCropControlUi(id: Int) {
        mCurrentStatusId = id
        mTvStatusRatio.setTextColor(if (id == R.id.tv_status_ratio) Color.BLUE else Color.WHITE)
        mTvStatusScale.setTextColor(if (id == R.id.tv_status_scale) Color.BLUE else Color.WHITE)
        mTvStatusRotate.setTextColor(if (id == R.id.tv_status_rotate) Color.BLUE else Color.WHITE)
    }

    private fun showWheel(isShow: Boolean) {
        mLlAspectRatio.visibility = if (isShow) GONE else VISIBLE
        mTvValue.visibility = if (!isShow) INVISIBLE else VISIBLE
        mIvAngle.visibility = if (!isShow) INVISIBLE else VISIBLE
        mIvReset.visibility = if (!isShow) INVISIBLE else VISIBLE
        mProgressWheelView.visibility = if (!isShow) INVISIBLE else VISIBLE
    }
}