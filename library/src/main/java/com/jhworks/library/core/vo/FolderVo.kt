package com.jhworks.library.core.vo

import android.text.TextUtils

/**
 * folder model data
 * @author jackson
 * @version 1.0
 * @date 2020/6/10 18:15
 */
data class FolderVo(var name: String? = null,
                    var path: String? = null,
                    var cover: MediaVo? = null) {
    var mediaStoreList: MutableList<MediaVo>? = null

    override fun equals(o: Any?): Boolean {
        try {
            val other = o as FolderVo
            return TextUtils.equals(other.path, path)
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
        return super.equals(o)
    }

    override fun toString(): String {
        return "FolderVo(name=$name, path=$path, cover=$cover, mediaStoreList=$mediaStoreList)"
    }
}