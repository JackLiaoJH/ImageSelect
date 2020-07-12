package com.jhworks.imageselect.crop

import android.app.Activity
import android.content.Intent
import android.net.Uri

/**
 * 裁剪入口
 * @author jackson
 * @version 1.0
 * @date 2020/7/7 14:17
 */
object ImageCrop {

    fun startAction(activity: Activity?, requestCode: Int, path: String) {
        activity ?: return
        activity.startActivityForResult(Intent(activity, com.jhworks.imageselect.crop.ImageCropActivity::class.java)
                .apply {
                    putExtra("path", path)
                }, requestCode)
    }

    fun startAction(activity: Activity?, requestCode: Int, uri: Uri) {
        activity ?: return
        activity.startActivityForResult(Intent(activity, com.jhworks.imageselect.crop.ImageCropActivity::class.java)
                .apply {
                    putExtra("uri", uri)
                }, requestCode)
    }
}