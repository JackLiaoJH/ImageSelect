package com.jhworks.library.crop

import android.app.Activity
import android.content.Intent

/**
 * 裁剪入口
 * @author jackson
 * @version 1.0
 * @date 2020/7/7 14:17
 */
object ImageCrop {

    fun startAction(activity: Activity?, requestCode: Int, path: String) {
        activity ?: return
        activity.startActivityForResult(Intent(activity, ImageCropActivity::class.java)
                .apply {
                    putExtra("path", path)
                }, requestCode)
    }
}