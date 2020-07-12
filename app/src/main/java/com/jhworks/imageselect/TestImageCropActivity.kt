package com.jhworks.imageselect

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.jhworks.library.ImageSelector
import com.jhworks.library.core.MediaSelectConfig
import com.jhworks.library.core.ui.ImageBaseActivity
import com.jhworks.library.core.vo.SelectMode
import com.jhworks.imageselect.crop.ImageCrop


/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/7/7 15:55
 */
class TestImageCropActivity : ImageBaseActivity() {

    companion object {
        private const val REQUEST_IMAGE_SELECT = 20
        private const val REQUEST_IMAGE_CROP = 30
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test_image_crop)

        findViewById<View>(R.id.button_crop).setOnClickListener {
            ImageSelector.startImageAction(this, REQUEST_IMAGE_SELECT,
                    MediaSelectConfig.Builder()
                            .setSelectMode(SelectMode.MODE_SINGLE)
                            .setShowCamera(true)
                            .setPlaceholderResId(R.mipmap.ic_launcher)
                            .setMaxCount(1)
                            .build()
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_SELECT) {
            if (resultCode == Activity.RESULT_OK) {
                val selectPath = ImageSelector.getSelectUriResults(data) ?: return
                ImageCrop.startAction(this, REQUEST_IMAGE_CROP, selectPath[0])
            }
        } else if (requestCode == REQUEST_IMAGE_CROP) {
            if (resultCode == Activity.RESULT_OK) {

            }
        }
    }
}