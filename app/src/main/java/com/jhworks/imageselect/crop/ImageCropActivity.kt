package com.jhworks.imageselect.crop

import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.jhworks.imageselect.R
import com.jhworks.library.core.MediaConstant
import com.jhworks.library.core.ui.ImageBaseActivity
import com.jhworks.imageselect.crop.callback.BitmapCropCallback
import java.io.File

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/7/7 14:19
 */
class ImageCropActivity : ImageBaseActivity(), com.jhworks.imageselect.view.TransformImageView.TransformImageListener {

    private lateinit var mGestureCropImageView: com.jhworks.imageselect.view.GestureCropImageView
    private lateinit var mOverlayView: com.jhworks.imageselect.view.OverlayView
    private lateinit var mCropView: com.jhworks.imageselect.view.CropView

    private var mBlockingView: View? = null

    private var mShowLoader = true
    private val mCompressFormat = MediaConstant.DEFAULT_COMPRESS_FORMAT
    private val mCompressQuality = MediaConstant.DEFAULT_COMPRESS_QUALITY

    @DrawableRes
    private val mToolbarCropDrawable = R.drawable.ic_sl_crop_done
    private var mToolbarWidgetColor = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sl_image_crop)

        val uri = intent.getParcelableExtra<Uri>("uri")

        mToolbarWidgetColor = Color.WHITE

        setupAppBar(getString(R.string.sl_menu_crop))

        mCropView = findViewById(R.id.sl_crop_view)
        mGestureCropImageView = mCropView.getCropImageView()
        mOverlayView = mCropView.getOverlayView()

        mGestureCropImageView.setTransformImageListener(this)
        mOverlayView

        uri?.let {
            val outUri = Uri.fromFile(File(cacheDir, "crop.jpg"))
            mGestureCropImageView.setImageUri(it, outUri)
            Log.e("crop", "out:$outUri , in:$it")
        }
        addBlockingView()
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

    }

    override fun onScale(currentScale: Float) {

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
        findViewById<LinearLayout>(R.id.sl_crop_root).addView(mBlockingView)
    }
}