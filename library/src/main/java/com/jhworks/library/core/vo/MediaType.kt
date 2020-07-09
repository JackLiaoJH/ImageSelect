package com.jhworks.library.core.vo

import androidx.annotation.IntDef

/**
 *The type of data
 * @author jackson
 * @version 1.0
 * @date 2020/6/10 18:56
 */
@IntDef(MediaType.VIDEO, MediaType.IMAGE)
@Retention(AnnotationRetention.SOURCE)
annotation class MediaType {
    companion object {
        const val VIDEO = 100
        const val IMAGE = 101
    }
}

@IntDef(SelectMode.MODE_SINGLE, SelectMode.MODE_MULTI)
@Retention(AnnotationRetention.SOURCE)
annotation class SelectMode {
    companion object {
        // Single choice
        const val MODE_SINGLE = 0

        // Multi choice
        const val MODE_MULTI = 1
    }
}