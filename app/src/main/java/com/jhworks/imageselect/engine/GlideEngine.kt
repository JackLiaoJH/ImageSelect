package com.jhworks.imageselect.engine

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.jhworks.library.core.vo.MediaUiConfigVo
import com.jhworks.library.engine.IEngine

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/6/11 15:15
 */
class GlideEngine : IEngine {
    override fun loadImage(imageView: ImageView, uiConfig: MediaUiConfigVo) {
        Glide.with(imageView)
                .load(uiConfig.path)
                .placeholder(uiConfig.placeholderResId)
                .error(uiConfig.errorResId)
                .override(uiConfig.width, uiConfig.height)
                .centerCrop()
                .into(imageView)
    }
}