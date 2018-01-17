package com.jhworks.library;

/**
 * User ：LiaoJH <br/>
 * Desc ： 常量
 * <br/>
 * Date ：2016/7/30 0030 <br/>
 */
public interface Constant {

    // Default image size
    int DEFAULT_IMAGE_SIZE = 9;
    // Default image span count
    int DEFAULT_IMAGE_SPAN_COUNT = 4;

    /**
     * Max image size，int，{@link #DEFAULT_IMAGE_SIZE} by default
     */
    String KEY_EXTRA_SELECT_COUNT = "max_select_count";

    /**
     * Result data set，ArrayList&lt;String&gt;
     */
    String KEY_EXTRA_RESULT = "select_result";

    String KEY_EXTRA_IMAGE_SELECT_LIST = "image_select_list";
    /**
     * Current Position
     */
    String KEY_EXTRA_CURRENT_POSITION = "current_position";

    String KEY_MEDIA_SELECT_CONFIG = "media_select_config";
}
