package com.jhworks.library.core.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.jhworks.library.R
import com.jhworks.library.core.MediaConstant
import com.jhworks.library.core.MediaConstant.KEY_MEDIA_SELECT_CONFIG
import com.jhworks.library.core.MediaSelectConfig
import com.jhworks.library.core.vo.MediaConfigVo
import com.jhworks.library.core.vo.MediaType
import com.jhworks.library.core.vo.SelectMode
import java.io.File

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/6/11 9:59
 */
class ImageSelectActivity : ImagePermissionActivity(), ImageSelectorFragment.Callback {

    companion object {

        private fun createIntent(context: Context, mediaConfig: MediaConfigVo?): Intent? {
            val intent = Intent(context, ImageSelectActivity::class.java)
            intent.putExtra(KEY_MEDIA_SELECT_CONFIG, mediaConfig)
            return intent
        }

        /**
         * start image select
         *
         * @param activity          -
         * @param requestCode       -
         * @param mediaSelectConfig -
         */
        fun start(activity: Activity?, requestCode: Int, mediaSelectConfig: MediaSelectConfig?) {
            if (activity == null || activity.isFinishing) return
            mediaSelectConfig ?: return
            activity.startActivityForResult(createIntent(activity,
                    MediaConfigVo.conver(mediaSelectConfig)), requestCode)
        }

        /**
         * start image select
         *
         * @param fragment          -
         * @param requestCode       -
         * @param mediaSelectConfig -
         */
        fun start(fragment: Fragment?, requestCode: Int, mediaSelectConfig: MediaSelectConfig?) {
            if (fragment == null) return
            val activity: Activity? = fragment.activity
            if (activity == null || activity.isFinishing) return
            mediaSelectConfig ?: return

            activity.startActivityForResult(createIntent(activity,
                    MediaConfigVo.conver(mediaSelectConfig)), requestCode)
        }
    }

    private var mResultList = arrayListOf<String>()
    private lateinit var mSubmitButton: Button
    private var mMediaConfig: MediaConfigVo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mMediaConfig = intent.getParcelableExtra(KEY_MEDIA_SELECT_CONFIG)

        if (mMediaConfig == null) {
            finish()
            return
        }

        requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE)

        setTheme(R.style.MIS_NO_ACTIONBAR)
        setContentView(R.layout.activity_image_select)
        mToolbar = findViewById(R.id.toolbar)
        mSubmitButton = findViewById(R.id.commit)
        mToolbar?.setTitle(
                if (mMediaConfig?.mediaType == MediaType.IMAGE)
                    R.string.select_phone
                else
                    R.string.select_video)
        initToolBar(true)

        if (mMediaConfig!!.selectMode == SelectMode.MODE_MULTI) {
            if (mMediaConfig!!.originData != null) {
                mResultList.clear()
                mResultList.addAll(mMediaConfig!!.originData!!)
            }

            updateDoneText(mResultList)
            mSubmitButton.visibility = View.VISIBLE
            mSubmitButton.setOnClickListener {
                if (mResultList.isNotEmpty()) {
                    // Notify success
                    val data = Intent()
                    data.putStringArrayListExtra(MediaConstant.KEY_EXTRA_RESULT, mResultList)
                    setResult(Activity.RESULT_OK, data)
                } else {
                    setResult(Activity.RESULT_CANCELED)
                }
                finish()
            }
        } else {
            mSubmitButton.visibility = View.GONE
        }
    }

    override fun onRequestPermissionSuccess() {
        val bundle = Bundle()
        bundle.putParcelable(KEY_MEDIA_SELECT_CONFIG, mMediaConfig)
        supportFragmentManager.beginTransaction()
                .add(R.id.image_grid, Fragment.instantiate(this,
                        ImageSelectorFragment::class.java.name, bundle))
                .commit()
    }

    override fun onRequestPermissionFail(deniedPermissions: MutableList<String>) {
        finish()
    }

    override fun onBackIconClick() {
        super.onBackIconClick()
        setResult(RESULT_CANCELED)
    }

    /**
     * Update done button by select image data
     *
     * @param resultList selected image data
     */
    private fun updateDoneText(resultList: MutableList<String>?) {
        var size = 0
        if (resultList == null || resultList.size <= 0) {
            mSubmitButton.setText(R.string.mis_action_done)
            mSubmitButton.isEnabled = false
        } else {
            size = resultList.size
            mSubmitButton.isEnabled = true
        }
        mSubmitButton.text = getString(R.string.mis_action_button_string,
                getString(R.string.mis_action_done), size, mMediaConfig?.maxCount ?: 0)
    }

    override fun onSingleImageSelected(path: String?) {
        path ?: return

        val data = Intent()
        mResultList.add(path)
        data.putStringArrayListExtra(MediaConstant.KEY_EXTRA_RESULT, mResultList)
        setResult(RESULT_OK, data)
        finish()
    }

    override fun onImageSelected(path: String?) {
        path ?: return

        if (!mResultList.contains(path)) {
            mResultList.add(path)
        }
        updateDoneText(mResultList)
    }

    override fun onImageUnselected(path: String?) {
        path ?: return

        if (mResultList.contains(path)) {
            mResultList.remove(path)
        }
        updateDoneText(mResultList)
    }

    override fun onCameraShot(imageFile: File?) {
        imageFile ?: return

        //notify system the image has change
        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)))

        val data = Intent()
        mResultList.add(imageFile.absolutePath)
        data.putStringArrayListExtra(MediaConstant.KEY_EXTRA_RESULT, mResultList)
        setResult(RESULT_OK, data)
        finish()
    }

    override fun onImageSelectList(imageList: MutableList<String>?) {
        imageList ?: return

        if (mResultList.isNotEmpty()) mResultList.clear()
        mResultList.addAll(imageList)
        updateDoneText(mResultList)
    }

}