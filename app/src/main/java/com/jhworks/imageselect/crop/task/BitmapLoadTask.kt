package com.jhworks.imageselect.crop.task

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import com.jhworks.imageselect.crop.callback.BitmapLoadCallback
import com.jhworks.imageselect.crop.vo.ExifInfo
import com.jhworks.imageselect.utils.BitmapLoadUtils
import java.io.*

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/7/7 18:30
 */
class BitmapLoadTask(private val context: Context,
                     private var inputUri: Uri,
                     private val outputUri: Uri,
                     private val requiredWidth: Int,
                     private val requiredHeight: Int,
                     private val loadCallback: BitmapLoadCallback?)
    : AsyncTask<Void, Void, BitmapLoadTask.BitmapWorkerResult>() {

    companion object {
        private const val TAG = "BitmapWorkerTask"
    }

    data class BitmapWorkerResult(var bitmapResult: Bitmap? = null,
                                  var exifInfo: ExifInfo? = null,
                                  var ex: Exception? = null)

    override fun doInBackground(vararg params: Void?): BitmapWorkerResult {
        try {
            processInputUri()
        } catch (e: NullPointerException) {
            return BitmapWorkerResult(ex = e)
        } catch (e: IOException) {
            return BitmapWorkerResult(ex = e)
        }

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inSampleSize = BitmapLoadUtils.calculateInSampleSize(options, requiredWidth, requiredHeight)
        options.inJustDecodeBounds = false

        var decodeSampledBitmap: Bitmap? = null

        var decodeAttemptSuccess = false
        while (!decodeAttemptSuccess) {
            try {
                val stream = context.contentResolver.openInputStream(inputUri)
                try {
                    decodeSampledBitmap = BitmapFactory.decodeStream(stream, null, options)
                    if (options.outWidth == -1 || options.outHeight == -1) {
                        return BitmapWorkerResult(
                                ex = IllegalArgumentException("Bounds for bitmap could not be retrieved from the Uri: [$inputUri]"))
                    }
                } finally {
                    BitmapLoadUtils.close(stream)
                }
                decodeAttemptSuccess = true
            } catch (error: OutOfMemoryError) {
                Log.e(TAG, "doInBackground: BitmapFactory.decodeFileDescriptor: ", error)
                options.inSampleSize *= 2
            } catch (e: IOException) {
                Log.e(TAG, "doInBackground: ImageDecoder.createSource: ", e)
                return BitmapWorkerResult(ex = IllegalArgumentException("Bitmap could not be decoded from the Uri: [$inputUri]", e))
            }
        }

        if (decodeSampledBitmap == null) {
            return BitmapWorkerResult(ex = IllegalArgumentException("Bitmap could not be decoded from the Uri: [$inputUri]"))
        }

        val exifOrientation: Int = BitmapLoadUtils.getExifOrientation(context, inputUri)
        val exifDegrees: Int = BitmapLoadUtils.exifToDegrees(exifOrientation)
        val exifTranslation: Int = BitmapLoadUtils.exifToTranslation(exifOrientation)

        val exifInfo = ExifInfo(exifOrientation, exifDegrees, exifTranslation)

        val matrix = Matrix()
        if (exifDegrees != 0) {
            matrix.preRotate(exifDegrees.toFloat())
        }
        if (exifTranslation != 1) {
            matrix.postScale(exifTranslation.toFloat(), 1f)
        }
        return if (!matrix.isIdentity) {
            BitmapWorkerResult(BitmapLoadUtils.transformBitmap(decodeSampledBitmap, matrix), exifInfo)
        } else BitmapWorkerResult(decodeSampledBitmap, exifInfo)

    }


    @Throws(NullPointerException::class, IOException::class)
    private fun processInputUri() {
        val inputUriScheme: String = inputUri.scheme ?: ""
        Log.d(TAG, "Uri scheme: $inputUriScheme")
        if ("content" == inputUriScheme) {
            try {
                copyFile()
            } catch (e: java.lang.NullPointerException) {
                Log.e(TAG, "Copying failed", e)
                throw e
            } catch (e: IOException) {
                Log.e(TAG, "Copying failed", e)
                throw e
            }
        } else if ("file" != inputUriScheme) {
            Log.e(TAG, "Invalid Uri scheme $inputUriScheme")
            throw java.lang.IllegalArgumentException("Invalid Uri scheme$inputUriScheme")
        }
    }

    @Throws(NullPointerException::class, IOException::class)
    private fun copyFile() {
        Log.d(TAG, "copyFile")
        outputUri.path ?: return

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(inputUri)
            outputStream = FileOutputStream(File(outputUri.path!!))
            if (inputStream == null) {
                throw NullPointerException("InputStream for given input Uri is null")
            }
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
        } finally {
            BitmapLoadUtils.close(outputStream)
            BitmapLoadUtils.close(inputStream)

            // swap uris, because input image was copied to the output destination
            // (cropped image will override it later)
            inputUri = outputUri
        }
    }

    override fun onPostExecute(result: BitmapWorkerResult) {
        if (result.ex == null) {
            if (result.bitmapResult == null || result.exifInfo == null) return
            loadCallback?.onBitmapLoaded(result.bitmapResult!!, result.exifInfo!!, inputUri.path!!, outputUri.path!!)
        } else {
            loadCallback?.onFailure(result.ex!!)
        }
    }
}