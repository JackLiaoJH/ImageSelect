package com.jhworks.imageselect.crop.task

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.jhworks.imageselect.crop.callback.BitmapCropCallback
import java.io.File
import java.io.IOException
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/7/7 18:00
 */
class BitmapCropTask(
        private var viewBitmap: Bitmap?,
        imageState: com.jhworks.imageselect.crop.vo.ImageState,
        cropParameters: com.jhworks.imageselect.crop.vo.CropParameters,
        private val cropCallback: BitmapCropCallback?) : AsyncTask<Void, Void, Throwable>() {

    companion object {
        private val TAG = BitmapCropTask::class.java.simpleName

    }

    private var mCropRect: RectF = imageState.cropRect
    private var mCurrentImageRect: RectF = imageState.currentImageRect

    private var mCurrentScale = imageState.currentScale
    private var mCurrentAngle = imageState.currentAngle
    private var mMaxResultImageSizeX = cropParameters.maxResultImageSizeX
    private var mMaxResultImageSizeY = cropParameters.maxResultImageSizeY

    private var mCompressFormat: CompressFormat = cropParameters.compressFormat
    private var mCompressQuality = cropParameters.compressQuality
    private var mImageInputPath: String? = cropParameters.imageInputPath
    private var mImageOutputPath: String? = cropParameters.imageOutputPath
    private var mExifInfo: com.jhworks.imageselect.crop.vo.ExifInfo? = cropParameters.exifInfo

    private var mCroppedImageWidth = 0
    private var mCroppedImageHeight: Int = 0
    private var cropOffsetX = 0
    private var cropOffsetY: Int = 0


    override fun doInBackground(vararg params: Void?): Throwable? {
        when {
            viewBitmap == null -> {
                return NullPointerException("ViewBitmap is null")
            }
            viewBitmap!!.isRecycled -> {
                return NullPointerException("ViewBitmap is recycled")
            }
            mCurrentImageRect!!.isEmpty -> {
                return NullPointerException("CurrentImageRect is empty")
            }
            else -> {
                val resizeScale = resize()
                viewBitmap = try {
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
        var scaleX = (if (swapSides) options.outHeight else options.outWidth) / viewBitmap!!.width.toFloat()
        var scaleY = (if (swapSides) options.outWidth else options.outHeight) / viewBitmap!!.height.toFloat()
        var resizeScale = scaleX.coerceAtMost(scaleY)
        mCurrentScale /= resizeScale
        resizeScale = 1f
        if (mMaxResultImageSizeX > 0 && mMaxResultImageSizeY > 0) {
            val cropWidth = mCropRect.width() / mCurrentScale
            val cropHeight = mCropRect.height() / mCurrentScale
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
        cropOffsetX = ((mCropRect.left - mCurrentImageRect.left) / mCurrentScale).roundToInt()
        cropOffsetY = ((mCropRect.top - mCurrentImageRect.top) / mCurrentScale).roundToInt()
        mCroppedImageWidth = (mCropRect.width() / mCurrentScale).roundToInt()
        mCroppedImageHeight = (mCropRect.height() / mCurrentScale).roundToInt()
        val shouldCrop = shouldCrop(mCroppedImageWidth, mCroppedImageHeight)
        Log.i(TAG, "Should crop: $shouldCrop")

        return if (shouldCrop) {
            val cropped = cropCImg(mImageInputPath, mImageOutputPath,
                    cropOffsetX, cropOffsetY,
                    mCroppedImageWidth, mCroppedImageHeight,
                    mCurrentAngle, resizeScale,
                    mCompressFormat.ordinal, mCompressQuality,
                    mExifInfo?.exifDegrees ?: 0,
                    mExifInfo?.exifTranslation ?: 0)
            if (cropped && mCompressFormat == CompressFormat.JPEG) {
                com.jhworks.imageselect.utils.ImageHeaderParser.copyExif(originalExif, mCroppedImageWidth, mCroppedImageHeight, mImageOutputPath!!)
            }
            cropped
        } else {
            com.jhworks.imageselect.utils.FileUtils.copyFile(mImageInputPath!!, mImageOutputPath!!)
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
        pixelError += (width.coerceAtLeast(height) / 1000f).roundToInt()
        return (mMaxResultImageSizeX > 0 && mMaxResultImageSizeY > 0
                || abs(mCropRect.left - mCurrentImageRect.left) > pixelError
                || abs(mCropRect.top - mCurrentImageRect.top) > pixelError
                || abs(mCropRect.bottom - mCurrentImageRect.bottom) > pixelError
                || abs(mCropRect.right - mCurrentImageRect.right) > pixelError
                || mCurrentAngle != 0f)
    }

    @Throws(IOException::class, OutOfMemoryError::class)
    external fun cropCImg(inputPath: String?, outputPath: String?,
                          left: Int, top: Int, width: Int, height: Int,
                          angle: Float, resizeScale: Float,
                          format: Int, quality: Int,
                          exifDegrees: Int, exifTranslation: Int): Boolean

    override fun onPostExecute(t: Throwable?) {

        if (t == null && mImageOutputPath != null) {
            val uri = Uri.fromFile(File(mImageOutputPath!!))
            cropCallback?.onBitmapCropped(uri, cropOffsetX, cropOffsetY, mCroppedImageWidth, mCroppedImageHeight)
        } else {
            cropCallback?.onCropFailure(t!!)
        }
    }
}