package com.jhworks.library.crop.task

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.jhworks.library.crop.callback.BitmapCropCallback
import com.jhworks.library.crop.vo.CropParameters
import com.jhworks.library.crop.vo.ExifInfo
import com.jhworks.library.crop.vo.ImageState
import com.jhworks.library.utils.FileUtils
import com.jhworks.library.utils.ImageHeaderParser
import java.io.File
import java.io.IOException

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/7/7 18:00
 */
class BitmapCropTask(
        viewBitmap: Bitmap, imageState: ImageState,
        cropParameters: CropParameters, cropCallback: BitmapCropCallback?)
    : AsyncTask<Void, Void, Throwable>() {

    companion object {
        private const val TAG = "BitmapCropTask"
//       System.loadLibrary("ucrop")
    }

    private var mViewBitmap: Bitmap? = null

    private var mCropRect: RectF? = null
    private var mCurrentImageRect: RectF? = null

    private var mCurrentScale = 0f
    private var mCurrentAngle: Float = 0f
    private var mMaxResultImageSizeX = 0
    private var mMaxResultImageSizeY = 0

    private var mCompressFormat: CompressFormat? = null
    private var mCompressQuality = 0
    private var mImageInputPath: String? = null
    private var mImageOutputPath: String? = null
    private var mExifInfo: ExifInfo? = null
    private var mCropCallback: BitmapCropCallback? = null

    private var mCroppedImageWidth = 0
    private var mCroppedImageHeight: Int = 0
    private var cropOffsetX = 0
    private var cropOffsetY: Int = 0

    init {
        mViewBitmap = viewBitmap
        mCropRect = imageState.cropRect
        mCurrentImageRect = imageState.currentImageRect

        mCurrentScale = imageState.currentScale
        mCurrentAngle = imageState.currentAngle
        mMaxResultImageSizeX = cropParameters.maxResultImageSizeX
        mMaxResultImageSizeY = cropParameters.maxResultImageSizeY

        mCompressFormat = cropParameters.compressFormat
        mCompressQuality = cropParameters.compressQuality

        mImageInputPath = cropParameters.imageInputPath
        mImageOutputPath = cropParameters.imageOutputPath
        mExifInfo = cropParameters.exifInfo

        mCropCallback = cropCallback
    }

    override fun doInBackground(vararg params: Void?): Throwable? {
        when {
            mViewBitmap == null -> {
                return NullPointerException("ViewBitmap is null")
            }
            mViewBitmap!!.isRecycled -> {
                return NullPointerException("ViewBitmap is recycled")
            }
            mCurrentImageRect!!.isEmpty -> {
                return NullPointerException("CurrentImageRect is empty")
            }
            else -> {
                val resizeScale = resize()
                mViewBitmap = try {
                    crop(resizeScale)
                    null
                } catch (throwable: Throwable) {
                    return throwable
                }
                return null
            }
        }
    }

    private fun resize(): Float {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(mImageInputPath, options)
        val swapSides = mExifInfo?.exifDegrees == 90 || mExifInfo?.exifDegrees == 270
        var scaleX = (if (swapSides) options.outHeight else options.outWidth) / mViewBitmap!!.width.toFloat()
        var scaleY = (if (swapSides) options.outWidth else options.outHeight) / mViewBitmap!!.height.toFloat()
        var resizeScale = scaleX.coerceAtMost(scaleY)
        mCurrentScale /= resizeScale
        resizeScale = 1f
        if (mMaxResultImageSizeX > 0 && mMaxResultImageSizeY > 0) {
            val cropWidth = mCropRect!!.width() / mCurrentScale
            val cropHeight = mCropRect!!.height() / mCurrentScale
            if (cropWidth > mMaxResultImageSizeX || cropHeight > mMaxResultImageSizeY) {
                scaleX = mMaxResultImageSizeX / cropWidth
                scaleY = mMaxResultImageSizeY / cropHeight
                resizeScale = scaleX.coerceAtMost(scaleY)
                mCurrentScale /= resizeScale
            }
        }
        return resizeScale
    }

    @Throws(IOException::class)
    private fun crop(resizeScale: Float): Boolean {
        if (mImageInputPath == null || mImageOutputPath == null) return false
        val originalExif = ExifInterface(mImageInputPath!!)
        cropOffsetX = Math.round((mCropRect!!.left - mCurrentImageRect!!.left) / mCurrentScale)
        cropOffsetY = Math.round((mCropRect!!.top - mCurrentImageRect!!.top) / mCurrentScale)
        mCroppedImageWidth = Math.round(mCropRect!!.width() / mCurrentScale)
        mCroppedImageHeight = Math.round(mCropRect!!.height() / mCurrentScale)
        val shouldCrop = shouldCrop(mCroppedImageWidth, mCroppedImageHeight)
        Log.i(TAG, "Should crop: $shouldCrop")
        return if (shouldCrop) {
            val cropped = cropCImg(mImageInputPath, mImageOutputPath,
                    cropOffsetX, cropOffsetY, mCroppedImageWidth, mCroppedImageHeight,
                    mCurrentAngle, resizeScale, mCompressFormat!!.ordinal, mCompressQuality,
                    mExifInfo?.exifDegrees ?: 0, mExifInfo?.exifTranslation ?: 0)
            if (cropped && mCompressFormat == CompressFormat.JPEG) {
                ImageHeaderParser.copyExif(originalExif, mCroppedImageWidth, mCroppedImageHeight, mImageOutputPath!!)
            }
            cropped
        } else {
            FileUtils.copyFile(mImageInputPath!!, mImageOutputPath!!)
            false
        }
    }

    /**
     * Check whether an image should be cropped at all or just file can be copied to the destination path.
     * For each 1000 pixels there is one pixel of error due to matrix calculations etc.
     *
     * @param width  - crop area width
     * @param height - crop area height
     * @return - true if image must be cropped, false - if original image fits requirements
     */
    private fun shouldCrop(width: Int, height: Int): Boolean {
        var pixelError = 1
        pixelError += Math.round(Math.max(width, height) / 1000f)
        return (mMaxResultImageSizeX > 0 && mMaxResultImageSizeY > 0
                || Math.abs(mCropRect!!.left - mCurrentImageRect!!.left) > pixelError || Math.abs(mCropRect!!.top - mCurrentImageRect!!.top) > pixelError || Math.abs(mCropRect!!.bottom - mCurrentImageRect!!.bottom) > pixelError || Math.abs(mCropRect!!.right - mCurrentImageRect!!.right) > pixelError || mCurrentAngle != 0f)
    }

    @Throws(IOException::class, OutOfMemoryError::class)
    external fun cropCImg(inputPath: String?, outputPath: String?,
                          left: Int, top: Int, width: Int, height: Int,
                          angle: Float, resizeScale: Float,
                          format: Int, quality: Int,
                          exifDegrees: Int, exifTranslation: Int): Boolean

    override fun onPostExecute(t: Throwable?) {
        if (mCropCallback != null) {
            if (t == null) {
                val uri = Uri.fromFile(File(mImageOutputPath))
                mCropCallback!!.onBitmapCropped(uri, cropOffsetX, cropOffsetY, mCroppedImageWidth, mCroppedImageHeight)
            } else {
                mCropCallback!!.onCropFailure(t)
            }
        }
    }
}