package com.jhworks.library

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.jhworks.library.core.MediaConstant
import com.jhworks.library.core.MediaSelectConfig
import com.jhworks.library.core.ui.ImageSelectActivity
import com.jhworks.library.core.vo.MediaType
import com.jhworks.library.engine.IEngine
import com.jhworks.library.utils.CheckNullUtils

/**
 * 图片选择器
 *
 * @author jackson
 * date 2020/6/11 11:04
 */
object ImageSelector {
    private var imageEngine: IEngine? = null

    fun startImageAction(activity: Activity?, requestCode: Int, config: MediaSelectConfig) {
        activity ?: return
        config.mediaType = MediaType.IMAGE
        ImageSelectActivity.start(activity, requestCode, config)
    }

    fun startImageAction(activity: FragmentActivity?, requestCode: Int, config: MediaSelectConfig) {
        activity ?: return
        config.mediaType = MediaType.IMAGE
        ImageSelectActivity.start(activity, requestCode, config)
    }

    fun startImageAction(fragment: Fragment?, requestCode: Int, config: MediaSelectConfig) {
        fragment ?: return
        config.mediaType = MediaType.IMAGE
        ImageSelectActivity.start(fragment, requestCode, config)
    }

    fun startVideoAction(activity: Activity?, requestCode: Int, config: MediaSelectConfig) {
        activity ?: return
        config.mediaType = MediaType.VIDEO
        ImageSelectActivity.start(activity, requestCode, config)
    }

    fun startVideoAction(activity: FragmentActivity?, requestCode: Int, config: MediaSelectConfig) {
        activity ?: return
        config.mediaType = MediaType.VIDEO
        ImageSelectActivity.start(activity, requestCode, config)
    }

    fun startVideoAction(fragment: Fragment?, requestCode: Int, config: MediaSelectConfig) {
        fragment ?: return
        config.mediaType = MediaType.VIDEO
        ImageSelectActivity.start(fragment, requestCode, config)
    }

    fun setImageEngine(imageEngine: IEngine): ImageSelector {
        this.imageEngine = imageEngine
        return this
    }

    fun getImageEngine(): IEngine {
        CheckNullUtils.check(imageEngine, "you must call setImageEngine() first !")
        return imageEngine!!
    }

    /**
     * 获取结果数据
     * @param data Intent
     * @return 选择结果数据
     */
    fun getSelectResults(data: Intent?): ArrayList<String>? {
        data ?: return null
        return data.getStringArrayListExtra(MediaConstant.KEY_EXTRA_RESULT)
    }
}