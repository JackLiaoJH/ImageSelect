package com.jhworks.library.core.callback

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2021/9/10 15:28
 */
interface ImgProgressListener {
    fun onLoadStart()
    fun onLoadProgress(progress: Int, isSuccess: Boolean)
    fun onLoadFail()
}