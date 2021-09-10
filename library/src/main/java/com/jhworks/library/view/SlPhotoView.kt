package com.jhworks.library.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.github.chrisbanes.photoview.PhotoView
import com.jhworks.library.core.callback.ImgProgressListener
import com.jhworks.library.core.vo.ImageInfoVo
import java.io.File
import java.lang.ref.WeakReference

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2021/9/9 17:53
 */
@SuppressLint("ViewConstructor")
class SlPhotoView(
    context: Context,
    private var position: Int,
    screenW: Int,
    private var imageInfoVo: ImageInfoVo
) : FrameLayout(context) {

    private var mPhotoView = PhotoView(context)
    private var mImageLoadingBar = SlImgLoadingBar(context, screenW shr 4)
    private var progressHandler: ProgressHandler? = ProgressHandler(this)

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        val pwLp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        pwLp.gravity = Gravity.CENTER
        mPhotoView.layoutParams = pwLp
        addView(mPhotoView)

        val loadingBarLp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        loadingBarLp.gravity = Gravity.CENTER
        addView(mImageLoadingBar, loadingBarLp)

        setListener(mPhotoView)
    }

    override fun onDetachedFromWindow() {
        progressHandler?.removeCallbacksAndMessages(null)
        progressHandler = null
        super.onDetachedFromWindow()
    }


    fun getPhotoView(): ImageView = mPhotoView
    fun getImgLoadingView(): SlImgLoadingBar = mImageLoadingBar

    fun setLongImageView(file: File, resource: Bitmap, url: String?) {
        val longImageView = SlLongImageView(context)
        longImageView.setData(file, resource, url)
        longImageView.tag = position
        setListener(longImageView)
        mPhotoView.visibility = GONE
        addView(
            longImageView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun setListener(view: View) {
        view.setOnClickListener { onImageClickListener?.onItemClick(imageInfoVo, position) }
        view.setOnLongClickListener {
            onImageClickListener?.onItemLongClick(imageInfoVo, position)
            return@setOnLongClickListener false
        }
//        if (view is PhotoView) (view as PhotoView).setOnViewTapListener {
//
//        }
    }

    var onImageClickListener: OnImageClickListener? = null


    interface OnImageClickListener {
        fun onItemClick(imageInfoVo: ImageInfoVo, pos: Int)
        fun onItemLongClick(imageInfoVo: ImageInfoVo, pos: Int)
//        fun onItemTapClick(imageInfoVo: ImageInfoVo, pos: Int)
    }

    fun getProgressListener(): ImgProgressListener = progressListener

    private val progressListener = object : ImgProgressListener {
        override fun onLoadStart() {
            Log.e("LIAO", "onLoadStart>>>>>")

            progressHandler?.sendEmptyMessage(PROGRESS_VISIBLE)
        }

        override fun onLoadProgress(progress: Int, isSuccess: Boolean) {
            Log.e("LIAO", "加载进度回调：${progress}, $isSuccess")
            progressHandler?.sendMessage(Message.obtain().apply {
                what = PROGRESS_NUM
                arg1 = progress
            })
        }

        override fun onLoadFail() {
            Log.e("LIAO", "onLoadFail：")
            progressHandler?.sendEmptyMessage(PROGRESS_GONE)
        }
    }

    companion object {
        const val PROGRESS_NUM = 0
        const val PROGRESS_GONE = 1
        const val PROGRESS_VISIBLE = 2

        class ProgressHandler(photoView: SlPhotoView) : Handler() {

            private val weakRef = WeakReference<SlPhotoView>(photoView)

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val slPhotoView = weakRef.get() ?: return
                when (msg.what) {
                    PROGRESS_NUM -> {
                        slPhotoView.mImageLoadingBar.setProgress(msg.arg1.toLong())
                    }
                    PROGRESS_GONE -> {
                        slPhotoView.mImageLoadingBar.visibility = GONE
                    }
                    PROGRESS_VISIBLE -> {
                        slPhotoView.mImageLoadingBar.visibility = VISIBLE
                    }
                }
            }
        }
    }
}