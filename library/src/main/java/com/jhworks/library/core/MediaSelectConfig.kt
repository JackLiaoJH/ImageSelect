package com.jhworks.library.core

import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import com.jhworks.library.R
import com.jhworks.library.core.vo.MediaType
import com.jhworks.library.core.vo.SelectMode

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/6/11 10:00
 */
class MediaSelectConfig private constructor(builder: Builder) {

    var isShowCamera = builder.isShowCamera
    var maxCount = builder.maxCount
    var imageSpanCount = builder.imageSpanCount
    var openCameraOnly = builder.openCameraOnly
    var originData = builder.originData

    @SelectMode
    var selectMode = builder.getSelectMode()

    @MediaType
    var mediaType = builder.getMediaType()

    @DrawableRes
    var errorResId = builder.errorResId

    @DrawableRes
    var placeholderResId = builder.placeholderResId

    @StyleRes
    var theme: Int = builder.theme

    class Builder {
        var isShowCamera: Boolean = false
        var maxCount = MediaConstant.DEFAULT_IMAGE_SIZE
        var imageSpanCount = MediaConstant.DEFAULT_IMAGE_SPAN_COUNT
        var openCameraOnly: Boolean = false
        var originData: ArrayList<String>? = null

        @DrawableRes
        var errorResId: Int = R.drawable.ic_sl_image_default

        @DrawableRes
        var placeholderResId: Int = R.drawable.ic_sl_image_default

        @SelectMode
        private var selectMode = SelectMode.MODE_MULTI

        @MediaType
        private var mediaType = MediaType.IMAGE

        @StyleRes
        var theme: Int = R.style.sl_theme_light

        fun build(): MediaSelectConfig {
            return MediaSelectConfig(this)
        }

        fun setShowCamera(showCamera: Boolean): Builder {
            isShowCamera = showCamera
            return this
        }

        /**
         * set select multi count
         *
         * @param maxCount select multi count
         * @return -
         */
        fun setMaxCount(maxCount: Int): Builder {
            this.maxCount = maxCount
            return this
        }

        /**
         * set select media mode
         *
         * @param selectMode 选择模式
         * @return -
         */
        fun setSelectMode(@SelectMode selectMode: Int): Builder {
            this.selectMode = selectMode
            return this
        }

        /**
         * set origin image list resource
         *
         * @param originData origin image list resource
         * @return -
         */
        fun setOriginData(originData: ArrayList<String>?): Builder {
            this.originData = originData
            return this
        }

        /**
         * image span count
         *
         * @param imageSpanCount span count ,default:4
         * @return -
         */
        fun setImageSpanCount(imageSpanCount: Int): Builder {
            this.imageSpanCount = imageSpanCount
            return this
        }

        fun setOpenCameraOnly(openCameraOnly: Boolean): Builder {
            this.openCameraOnly = openCameraOnly
            return this
        }

        fun setMediaType(@MediaType mediaType: Int) {
            this.mediaType = mediaType
        }

        fun setErrorResId(@DrawableRes resId: Int): Builder {
            this.errorResId = resId
            return this
        }

        fun setPlaceholderResId(@DrawableRes resId: Int): Builder {
            this.placeholderResId = resId
            return this
        }

        /**
         * set theme ，default is R.style.sl_theme_light
         */
        fun setTheme(@StyleRes theme: Int): Builder {
            this.theme = theme
            return this
        }

        fun getSelectMode(): Int = selectMode
        fun getMediaType(): Int = mediaType
    }
}