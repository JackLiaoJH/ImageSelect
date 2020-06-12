package com.jhworks.library.core

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

    const val KEY_EXTRA_IMAGE_SELECT_LIST = "image_select_list"

    // all image list key
    const val KEY_EXTRA_ALL_IMAGE_LIST = "image_all_list"

    /**
     * Current Position
     */
    const val KEY_EXTRA_CURRENT_POSITION = "current_position"

    const val KEY_MEDIA_SELECT_CONFIG = "media_select_config"
}