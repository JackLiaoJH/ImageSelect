package com.jhworks.imageselect.okhttp

import android.util.Log
import com.jhworks.library.core.callback.ImgProgressListener
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2021/9/10 16:01
 */
class ProgressResponseBody(
    private val url: String,
    private val responseBody: ResponseBody?
) : ResponseBody() {

    private var progressListener: ImgProgressListener? = null
    private var bufferedSource: BufferedSource? = null

    init {
        if (ProgressInterceptor.LISTENER_MAP != null) {
            progressListener = ProgressInterceptor.LISTENER_MAP?.get(url)
        }
    }

    override fun contentType(): MediaType? = responseBody?.contentType()
    override fun contentLength(): Long = responseBody?.contentLength() ?: 0

    override fun source(): BufferedSource? {
        if (bufferedSource == null && responseBody != null) {
            bufferedSource = ProgressSource(responseBody, progressListener).buffer()
        }
        return bufferedSource
    }

    private class ProgressSource(
        private val responseBody: ResponseBody,
        private var progressListener: ImgProgressListener?
    ) :
        ForwardingSource(responseBody.source()) {
        var totalBytesRead: Long = 0
        var currentProgress = 0

        override fun read(sink: Buffer, byteCount: Long): Long {
            val bytesRead = super.read(sink, byteCount)
            val fullLength = responseBody.contentLength()
            if (bytesRead == -1L) {
                totalBytesRead = fullLength
            } else {
                totalBytesRead += bytesRead
            }
            val progress = (100f * totalBytesRead / fullLength).toInt()
            Log.e("LIAO", "加载进度：$progress ,$progressListener")
            if (progressListener != null && progress != currentProgress) {
                progressListener?.onLoadProgress(progress, progress == 100)
            }
            if (progressListener != null && totalBytesRead == fullLength) {
                progressListener = null
            }
            currentProgress = progress
            return bytesRead
        }
    }
}