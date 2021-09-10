package com.jhworks.imageselect.okhttp

import com.jhworks.library.core.callback.ImgProgressListener
import okhttp3.Interceptor
import okhttp3.Response

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2021/9/10 15:55
 */
class ProgressInterceptor : Interceptor {

    companion object {
        var LISTENER_MAP: HashMap<String, ImgProgressListener>? = null

        fun init() {
            if (LISTENER_MAP == null) LISTENER_MAP = HashMap()
        }

        fun clear() {
            LISTENER_MAP?.clear()
            LISTENER_MAP = null
        }

        fun addListener(url: String?, l: ImgProgressListener?) {
            url ?: return
            l ?: return
            init()
            LISTENER_MAP?.put(url, l)
        }

        fun removeListener(url: String?) {
            url ?: return
            LISTENER_MAP?.remove(url)
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val url = request.url().toString()
        val body = response.body()
        return response.newBuilder().body(ProgressResponseBody(url, body)).build()
    }
}