package com.jhworks.library.core.vo

import android.os.Parcel
import android.os.Parcelable

/**
 * Image info, contains normal url, small url (can null)
 * @author jackson
 * @version 1.0
 * @date 2021/9/9 18:23
 */
data class ImageInfoVo(
    var url: String?,
    var smallUrl: String? = null
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
        parcel.writeString(smallUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ImageInfoVo> {
        override fun createFromParcel(parcel: Parcel): ImageInfoVo {
            return ImageInfoVo(parcel)
        }

        override fun newArray(size: Int): Array<ImageInfoVo?> {
            return arrayOfNulls(size)
        }
    }

}