package com.jhworks.imageselect.crop.vo

import android.os.Parcel
import android.os.Parcelable

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/7/15 16:32
 */
data class AspectRatio(
        var aspectRatioTitle: String?,
        var aspectRatioX: Float = 0f,
        var aspectRatioY: Float = 0f
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readFloat(),
            parcel.readFloat())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(aspectRatioTitle)
        parcel.writeFloat(aspectRatioX)
        parcel.writeFloat(aspectRatioY)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AspectRatio> {
        override fun createFromParcel(parcel: Parcel): AspectRatio {
            return AspectRatio(parcel)
        }

        override fun newArray(size: Int): Array<AspectRatio?> {
            return arrayOfNulls(size)
        }
    }
}