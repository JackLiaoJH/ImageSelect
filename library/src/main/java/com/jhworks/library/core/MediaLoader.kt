package com.jhworks.library.core

import android.content.Context
import android.os.Bundle
import androidx.loader.content.AsyncTaskLoader
import com.jhworks.library.core.vo.FolderVo
import com.jhworks.library.core.vo.MediaConfigVo
import com.jhworks.library.core.vo.MediaType
import com.jhworks.library.core.vo.MediaVo


/**
 * Loads metadata from the media store for images and videos.
 *
 * @author jackson
 * @version 1.0
 * @date 2020/6/10 17:55
 */
class MediaLoader(context: Context, private val mediaId: Int, private val bundle: Bundle?, config: MediaConfigVo?)
    : AsyncTaskLoader<MutableList<MediaVo>>(context) {

    private var mCachedList: MutableList<MediaVo>? = null
    private var mObserverRegistered = false
    private val mForceLoadContentObserver = ForceLoadContentObserver()

    // folder result data set
    private val mResultFolder = arrayListOf<FolderVo>()

    @MediaType
    private var mMediaType = config?.mediaType ?: MediaType.IMAGE

    override fun onStartLoading() {
        super.onStartLoading()
        if (mCachedList != null) deliverResult(mCachedList)
        if (takeContentChanged() || mCachedList == null || mCachedList!!.isEmpty()) {
            forceLoad()
        }
        registerContentObserver()
    }

    override fun loadInBackground(): MutableList<MediaVo>? {
        val data =
                if (mMediaType == MediaType.IMAGE)
                    MediaParse.queryImages(context, mediaId, bundle, mResultFolder)
                else
                    MediaParse.queryVideos(context, mResultFolder)
        mCachedList = data
        return data
    }

    override fun onStopLoading() {
        super.onStopLoading()
        cancelLoad()
    }

    override fun deliverResult(data: MutableList<MediaVo>?) {
        if (!isReset && isStarted) super.deliverResult(data)
    }

    override fun onReset() {
        super.onReset()
        onStopLoading()
        mCachedList = null
        unregisterContentObserver()
    }

    override fun onAbandon() {
        super.onAbandon()
        unregisterContentObserver()
    }


    private fun registerContentObserver() {
        if (!mObserverRegistered) {
            context.contentResolver.registerContentObserver(
                    MediaParse.MEDIA_IMAGE_URI,
                    false,
                    mForceLoadContentObserver
            )
            context.contentResolver.registerContentObserver(
                    MediaParse.MEDIA_VIDEO_URI,
                    false,
                    mForceLoadContentObserver
            )
            mObserverRegistered = true
        }
    }

    private fun unregisterContentObserver() {
        if (mObserverRegistered) {
            mObserverRegistered = false
            context.contentResolver.unregisterContentObserver(mForceLoadContentObserver)
        }
    }

    fun getResultFolder(): MutableList<FolderVo> {
        return mResultFolder
    }
}