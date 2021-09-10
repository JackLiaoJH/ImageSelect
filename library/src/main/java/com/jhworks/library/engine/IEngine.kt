package com.jhworks.library.engine

import android.widget.ImageView
import com.jhworks.library.core.callback.ImgProgressListener
import com.jhworks.library.core.vo.MediaUiConfigVo
import com.jhworks.library.view.SlPhotoView

/**
 *  图片展示或视频预览逻辑接口
 * @author jackson
 * @version 1.0
 * @date 2020/6/11 15:11
 */
interface IEngine {
    /**正常列表加载*/
    fun loadImage(imageView: ImageView, uiConfig: MediaUiConfigVo)

    /**详情页加载*/
    fun loadDetailImage(imageView: ImageView, uiConfig: MediaUiConfigVo)

    fun loadBigImage(
        photoView: SlPhotoView,
        uiConfig: MediaUiConfigVo,
        progressListener: ImgProgressListener?
    )

}