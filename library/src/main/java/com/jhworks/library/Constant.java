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
    // Single choice
    int MODE_SINGLE = 0;
    // Multi choice
    int MODE_MULTI = 1;
    // Default image span count
    int DEFAULT_IMAGE_SPAN_COUNT = 4;

    /**
     * Max image size，int，{@link #DEFAULT_IMAGE_SIZE} by default
     */
    String KEY_EXTRA_SELECT_COUNT = "max_select_count";
    /**
     * Select mode，{@link #MODE_MULTI} by default
     */
    String KEY_EXTRA_SELECT_MODE = "select_count_mode";
    /**
     * Whether show camera，true by default
     */
    String KEY_EXTRA_SHOW_CAMERA = "show_camera";
    /**
     * Result data set，ArrayList&lt;String&gt;
     */
    String KEY_EXTRA_RESULT = "select_result";
    /**
     * Original data set
     */
    String KEY_EXTRA_DEFAULT_SELECTED_LIST = "default_list";
    /**
     * image span count
     */
    String KEY_EXTRA_IMAGE_SPAN_COUNT = "image_span_count";
    /**
     * Image List
     */
    String KEY_EXTRA_IMAGE_LIST = "image_list";
    String KEY_EXTRA_IMAGE_SELECT_LIST = "image_select_list";
    /**
     * Current Position
     */
    String KEY_EXTRA_CURRENT_POSITION = "current_position";
    /**
     * OPEN CAMERA ONLY
     */
    String KEY_EXTRA_OPEN_CAMERA_ONLY = "open_camera_only";
}
