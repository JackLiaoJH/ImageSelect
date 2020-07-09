package com.jhworks.library.core.vo

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils


/**
 * A data model containing data for a single media item.
 * @author jackson
 * @version 1.0
 * @date 2020/6/10 18:10
 */
data class MediaVo(
        val rowId: Long = 0,
        val uri: Uri? = null,
        val mimeType: String? = null,
        val dateModified: Long = 0,
        val orientation: Int = 0,
        val type: Int = 0,
        val dateTaken: Long = 0,
        val path: String? = null,
        val name: String? = null,
        val time: Long = 0,
        val size: Int = 0
) : Parcelable {
    var isSelect: Boolean = false

    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readParcelable(Uri::class.java.classLoader),
            parcel.readString(),
            parcel.readLong(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readLong(),
            parcel.readString(),
            parcel.readString(),
            parcel.readLong(),
            parcel.readInt()) {
        isSelect = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(rowId)
        parcel.writeParcelable(uri, flags)
        parcel.writeString(mimeType)
        parcel.writeLong(dateModified)
        parcel.writeInt(orientation)
        parcel.writeInt(type)
        parcel.writeLong(dateTaken)
        parcel.writeString(path)
        parcel.writeString(name)
        parcel.writeLong(time)
        parcel.writeInt(size)
        parcel.writeByte(if (isSelect) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val otherMedia = o as MediaVo

        return TextUtils.equals(path, otherMedia.path)
    }

    override fun toString(): String {
        return "MediaVo(rowId=$rowId, uri=$uri, mimeType=$mimeType, dateModified=$dateModified, orientation=$orientation, type=$type, dateTaken=$dateTaken, path=$path, name=$name, time=$time, size=$size, isSelect=$isSelect)"
    }

    companion object CREATOR : Parcelable.Creator<MediaVo> {
        override fun createFromParcel(parcel: Parcel): MediaVo {
            return MediaVo(parcel)
        }

        override fun newArray(size: Int): Array<MediaVo?> {
            return arrayOfNulls(size)
        }
    }
}