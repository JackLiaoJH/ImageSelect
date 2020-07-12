package com.jhworks.library.core

import android.graphics.Bitmap.CompressFormat
import com.jhworks.library.core.vo.MediaVo

/**
 * Media Constant
 * @author jackson
 * @version 1.0
 * @date 2020/6/11 10:15
 */
object MediaConstant {
    // Default image size
    const val DEFAULT_IMAGE_SIZE = 9

    // Default image span count
    const val DEFAULT_IMAGE_SPAN_COUNT = 4

    /**
     * Max image size，int，[.DEFAULT_IMAGE_SIZE] by default
     */
    const val KEY_EXTRA_SELECT_COUNT = "max_select_count"

    /**
     * Result data set，ArrayList&lt;String&gt;
     */
    const val KEY_EXTRA_RESULT = "select_result"

    /**
     * Result data set，MutableList&lt;Uri&gt;
     */
    const val KEY_EXTRA_RESULT_URI = "select_result_uri"

    /**
     * Current Position
     */
    const val KEY_EXTRA_CURRENT_POSITION = "current_position"

    const val KEY_MEDIA_SELECT_CONFIG = "media_select_config"

    const val RESULT_ERROR = 400

    //-------------------------crop-----------------------------//
    const val DEFAULT_COMPRESS_QUALITY = 90
    val DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG

    //all image list
    private val mAllMediaList = arrayListOf<MediaVo>()
    private val mSelectMediaList = arrayListOf<MediaVo>()

    fun setAllMediaList(list: MutableList<MediaVo>?) {
        if (mAllMediaList.isNotEmpty()) mAllMediaList.clear()
        if (list != null) mAllMediaList.addAll(list)
    }

    fun getAllMediaList(): MutableList<MediaVo> = mAllMediaList

    fun setSelectMediaList(list: MutableList<MediaVo>?) {
        if (mSelectMediaList.isNotEmpty()) mSelectMediaList.clear()
        if (list != null) mSelectMediaList.addAll(list)
    }

    fun getSelectMediaList(): MutableList<MediaVo> = mSelectMediaList

    fun clear() {
        mAllMediaList.clear()
        mSelectMediaList.clear()
    }
}