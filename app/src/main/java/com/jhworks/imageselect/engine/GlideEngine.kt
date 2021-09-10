package com.jhworks.imageselect.engine

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View.VISIBLE
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.jhworks.imageselect.okhttp.ProgressInterceptor
import com.jhworks.imageselect.utils.GlideUtils
import com.jhworks.library.core.callback.ImgProgressListener
import com.jhworks.library.core.vo.ImageInfoVo
import com.jhworks.library.core.vo.MediaUiConfigVo
import com.jhworks.library.engine.IEngine
import com.jhworks.library.utils.SlFileUtils
import com.jhworks.library.utils.SlImageUtils
import com.jhworks.library.view.SlPhotoView
import java.io.File

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/6/11 15:15
 */
class GlideEngine : IEngine {

    private val TAG = "GlideEngine"

    override fun loadImage(imageView: ImageView, uiConfig: MediaUiConfigVo) {
        Glide.with(imageView)
            .load(uiConfig.path)
            .placeholder(uiConfig.placeholderResId)
            .error(uiConfig.errorResId)
            .override(uiConfig.width, uiConfig.height)
            .centerCrop()
            .into(imageView)
    }

    override fun loadDetailImage(imageView: ImageView, uiConfig: MediaUiConfigVo) {
        Glide.with(imageView)
            .load(uiConfig.path)
            .placeholder(uiConfig.placeholderResId)
            .error(uiConfig.errorResId)
            .override(uiConfig.width, uiConfig.height)
            .fitCenter()
            .into(imageView)
    }


    private var isLongImage = false

    override fun loadBigImage(
        photoView: SlPhotoView,
        uiConfig: MediaUiConfigVo,
        progressListener: ImgProgressListener?
    ) {
        val imageView = photoView.getPhotoView()
        val bigImageInfo = uiConfig.imageInfoVo
        val bigImageUrl = bigImageInfo?.url
        ProgressInterceptor.addListener(bigImageUrl, progressListener)

        photoView.onImageClickListener = object : SlPhotoView.OnImageClickListener {
            override fun onItemClick(imageInfoVo: ImageInfoVo, pos: Int) {
                Log.e(TAG, "点击: ${imageInfoVo.url}, $pos")
            }

            override fun onItemLongClick(imageInfoVo: ImageInfoVo, pos: Int) {
                Log.e(TAG, "onItemLongClick: ${imageInfoVo.url}, $pos")
            }
        }
        photoView.onViewWindowChangeListener = object : SlPhotoView.OnViewWindowChangeListener {
            override fun onDetachedFromWindow() {
                removeProgressListener(bigImageUrl, progressListener)
            }
        }


        if (SlImageUtils.isGifOrWebp(bigImageUrl)) {
            loadGiftOrWebp(imageView, uiConfig, progressListener)
            return
        }

        Glide.with(imageView)
            .asBitmap()
            .load(bigImageUrl)
//            .thumbnail(Glide.with(photoView.context).asBitmap().load(bigImageInfo?.smallUrl))
            .error(uiConfig.errorResId)
//            .skipMemoryCache(true)
//            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    isLongImage = SlImageUtils.isLongImage(imageView.context, resource, bigImageUrl)

                    return false
                }

            })
            .fitCenter()
            .into(object : BitmapImageViewTarget(imageView) {
                override fun onLoadStarted(placeholder: Drawable?) {
                    super.onLoadStarted(placeholder)
                    progressListener?.onLoadStart()
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    removeProgressListener(bigImageUrl, progressListener, true)
                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    super.onResourceReady(resource, transition)
                    removeProgressListener(bigImageUrl, progressListener)
                    if (isLongImage) {
                        val cachePath =
                            GlideUtils.getGlideLocalCachePath(imageView.context, bigImageUrl)
//                        Log.e(TAG, "图片缓存地址:$cachePath")

                        if (SlFileUtils.isFileExits(cachePath)) {
                            val cacheFile = File(cachePath!!)
                            photoView.setLongImageView(cacheFile, resource, bigImageUrl)
                        }
                        return
                    }
                    imageView.visibility = VISIBLE
                    imageView.setImageBitmap(resource)
                }
            })
    }

    private fun removeProgressListener(
        url: String?,
        progressListener: ImgProgressListener?,
        isFail: Boolean = false
    ) {
        if (isFail) progressListener?.onLoadFail() else progressListener?.onLoadProgress(100, true)
        ProgressInterceptor.removeListener(url)
    }


    private fun loadGiftOrWebp(
        imageView: ImageView,
        uiConfig: MediaUiConfigVo,
        progressListener: ImgProgressListener?
    ) {
        val bigImageInfo = uiConfig.imageInfoVo
        val url = bigImageInfo?.url
        Glide.with(imageView)
            .asGif()
            .load(url)
//            .thumbnail(Glide.with(photoView.context).asBitmap().load(bigImageInfo?.smallUrl))
            .error(uiConfig.errorResId)
//            .diskCacheStrategy(DiskCacheStrategy.NONE)
//            .skipMemoryCache(true)
//            .override(uiConfig.width, uiConfig.height)
            .listener(object : RequestListener<GifDrawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    removeProgressListener(url, progressListener, true)
                    return false
                }

                override fun onResourceReady(
                    resource: GifDrawable?,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    removeProgressListener(url, progressListener)
                    return false
                }
            })
            .fitCenter()
            .into(imageView)
    }
}