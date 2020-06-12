package com.jhworks.library.core.vo

import androidx.annotation.DrawableRes
import com.jhworks.library.R

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/6/11 15:32
 */
class MediaUiConfigVo(
        var path: Any?, var width: Int = -1,
        var height: Int = -1,

        @DrawableRes
        var placeholderResId: Int = R.drawable.ic_image_default,

        @DrawableRes
        var errorResId: Int = R.drawable.ic_image_default) {
}