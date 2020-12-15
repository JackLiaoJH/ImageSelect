package com.jhworks.library.core.vo

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import com.jhworks.library.R
import com.jhworks.library.core.MediaConstant
import com.jhworks.library.core.MediaSelectConfig

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/6/11 10:31
 */
data class MediaConfigVo(
        var isShowCamera: Boolean = false,
        var maxCount: Int = MediaConstant.DEFAULT_IMAGE_SIZE,
        var imageSpanCount: Int = MediaConstant.DEFAULT_IMAGE_SPAN_COUNT,
        var openCameraOnly: Boolean = false,
        var originData: ArrayList<String>? = null,
        @SelectMode
        var selectMode: Int = SelectMode.MODE_MULTI,
        @MediaType
        var mediaType: Int = MediaType.IMAGE,
        @DrawableRes
        var errorResId: Int = R.drawable.ic_sl_image_default,
        @DrawableRes
        var placeholderResId: Int = R.drawable.ic_sl_image_default,
        @StyleRes
        var theme: Int = R.style.sl_theme_light
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readByte() != 0.toByte(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readByte() != 0.toByte(),
            parcel.createStringArrayList(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (isShowCamera) 1 else 0)
        parcel.writeInt(maxCount)
        parcel.writeInt(imageSpanCount)
        parcel.writeByte(if (openCameraOnly) 1 else 0)
        parcel.writeStringList(originData)
        parcel.writeInt(selectMode)
        parcel.writeInt(mediaType)
        parcel.writeInt(errorResId)
        parcel.writeInt(placeholderResId)
        parcel.writeInt(theme)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MediaConfigVo> {

        fun conver(config: MediaSelectConfig): MediaConfigVo {
            return MediaConfigVo(
                    config.isShowCamera,
                    config.maxCount,
                    config.imageSpanCount,
                    config.openCameraOnly,
                    config.originData,
                    config.selectMode,
                    config.mediaType,
                    config.errorResId,
                    config.placeholderResId,
                    config.theme
            )
        }

        override fun createFromParcel(parcel: Parcel): MediaConfigVo {
            return MediaConfigVo(parcel)
        }

        override fun newArray(size: Int): Array<MediaConfigVo?> {
            return arrayOfNulls(size)
        }
    }
}