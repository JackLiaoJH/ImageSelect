package com.jhworks.library.crop.task

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import com.jhworks.library.crop.callback.BitmapLoadCallback
import com.jhworks.library.crop.vo.ExifInfo
import com.jhworks.library.utils.BitmapLoadUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.BufferedSource
import okio.Okio
import okio.Sink
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

    class BitmapWorkerResult(bitmapResult: Bitmap? = null,
                             exifInfo: ExifInfo? = null,
                             ex: Exception? = null) {
        var mBitmapResult: Bitmap? = bitmapResult
        var mExifInfo: ExifInfo? = exifInfo
        var mBitmapWorkerException: Exception? = ex

    }

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


    @Throws(java.lang.NullPointerException::class, IOException::class)
    private fun processInputUri() {
        val inputUriScheme: String = inputUri?.scheme ?: ""
        Log.d(TAG, "Uri scheme: $inputUriScheme")
        if ("http" == inputUriScheme || "https" == inputUriScheme) {
            try {
                downloadFile()
            } catch (e: java.lang.NullPointerException) {
                Log.e(TAG, "Downloading failed", e)
                throw e
            } catch (e: IOException) {
                Log.e(TAG, "Downloading failed", e)
                throw e
            }
        } else if ("content" == inputUriScheme) {
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

    @Throws(java.lang.NullPointerException::class, IOException::class)
    private fun copyFile() {
        Log.d(TAG, "copyFile")
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(inputUri)
            outputStream = FileOutputStream(File(outputUri.path))
            if (inputStream == null) {
                throw java.lang.NullPointerException("InputStream for given input Uri is null")
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

    @Throws(java.lang.NullPointerException::class, IOException::class)
    private fun downloadFile() {
        Log.d(TAG, "downloadFile")

        val client = OkHttpClient()
        var source: BufferedSource? = null
        var sink: Sink? = null
        var response: Response? = null
        try {
            val request: Request = Request.Builder()
                    .url(inputUri.toString())
                    .build()
            response = client.newCall(request).execute()
            source = response.body()?.source()
            val outputStream = context.contentResolver.openOutputStream(outputUri)
            if (outputStream != null) {
                sink = Okio.sink(outputStream)
                source?.readAll(sink)
            } else {
                throw java.lang.NullPointerException("OutputStream for given output Uri is null")
            }
        } finally {
            BitmapLoadUtils.close(source)
            BitmapLoadUtils.close(sink)
            if (response != null) {
                BitmapLoadUtils.close(response.body())
            }
            client.dispatcher().cancelAll()

            // swap uris, because input image was downloaded to the output destination
            // (cropped image will override it later)
            inputUri = outputUri
        }
    }

    override fun onPostExecute(result: BitmapWorkerResult) {
        if (result.mBitmapWorkerException == null) {
            if (result.mBitmapResult == null || result.mExifInfo == null) return
            loadCallback?.onBitmapLoaded(result.mBitmapResult!!, result.mExifInfo!!, inputUri.path!!, outputUri.path!!)
        } else {
            loadCallback?.onFailure(result.mBitmapWorkerException!!)
        }
    }
}