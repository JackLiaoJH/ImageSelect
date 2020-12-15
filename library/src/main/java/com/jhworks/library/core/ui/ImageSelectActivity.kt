package com.jhworks.library.core.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.jhworks.library.R
import com.jhworks.library.core.MediaConstant
import com.jhworks.library.core.MediaConstant.KEY_MEDIA_SELECT_CONFIG
import com.jhworks.library.core.MediaSelectConfig
import com.jhworks.library.core.vo.MediaConfigVo
import com.jhworks.library.core.vo.MediaType
import com.jhworks.library.core.vo.MediaVo
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

    private lateinit var mSubmitButton: TextView
    private lateinit var mTvSelectCount: TextView
    private lateinit var mTvCenterTitle: TextView
    private lateinit var mLlFolder: LinearLayout
    private lateinit var mIvFolderArrow: ImageView
    private lateinit var mIvToolbarNav: ImageView

    private var imageSelectorFragment: ImageSelectorFragment? = null

    override fun setLayout(): Int = R.layout.activity_sl_image_select

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (mMediaConfig == null) {
            finish()
            return
        }
        requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        mSubmitButton = findViewById(R.id.sl_commit)
        mTvSelectCount = findViewById(R.id.tv_sl_select_image_count)
        mTvCenterTitle = findViewById(R.id.tv_sl_toolbar_folder)
        mLlFolder = findViewById(R.id.sl_ll_toolbar_folder)
        mIvFolderArrow = findViewById(R.id.sl_iv_toolbar_folder_arrow)
        mIvToolbarNav = findViewById(R.id.sl_ic_toolbar_nav)

        updateNavIcon(mIvToolbarNav.drawable)
        val titleResId = if (mMediaConfig?.mediaType == MediaType.IMAGE) R.string.sl_folder_image_all else R.string.sl_folder_video_all
        mTvCenterTitle.setText(titleResId)
        initToolBarConfig(false, false)

        if (mMediaConfig!!.selectMode == SelectMode.MODE_MULTI) {
            updateDoneText(mMediaConfig!!.originData?.size ?: 0)

            mSubmitButton.visibility = View.VISIBLE
            mTvSelectCount.visibility = View.VISIBLE
            mSubmitButton.setOnClickListener {
                val selectList = MediaConstant.getSelectMediaList()
                if (selectList.isNotEmpty()) {
                    // Notify success
                    dealResult()
                } else {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
        } else {
            mSubmitButton.visibility = View.GONE
            mTvSelectCount.visibility = View.GONE
        }
        mLlFolder.setOnClickListener {
            imageSelectorFragment?.showFolderPopWin(mToolbar!!, mTvCenterTitle, mIvFolderArrow)
        }
        mIvToolbarNav.setOnClickListener { finish() }
    }

    override fun onRequestPermissionSuccess() {
        imageSelectorFragment = ImageSelectorFragment.newInstance(mMediaConfig)
        supportFragmentManager.beginTransaction()
                .add(R.id.sl_image_grid, imageSelectorFragment!!)
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
     * @param selectCount selected image count
     */
    private fun updateDoneText(selectCount: Int) {
        mSubmitButton.isEnabled = selectCount > 0

//        mSubmitButton.text = getString(R.string.sl_action_button_string,
//                getString(R.string.sl_action_done), selectCount, mMediaConfig?.maxCount ?: 0)
        mTvSelectCount.text =
                getString(R.string.sl_select_image_count, selectCount, mMediaConfig?.maxCount ?: 0)
    }

    override fun onSingleImageSelected(media: MediaVo?) {
        if (media?.path == null || media.uri == null) return

        setIntentResult(arrayListOf(media.path), arrayListOf(media.uri))
    }

    override fun onImageSelected(path: String?) {
        path ?: return
        updateDoneText(MediaConstant.getSelectMediaList().size)
    }

    override fun onImageUnselected(path: String?) {
        path ?: return
        updateDoneText(MediaConstant.getSelectMediaList().size)
    }

    override fun onCameraShot(imageFile: File?) {
        imageFile ?: return

        val uriFile = Uri.fromFile(imageFile)
        //notify system the image has change
        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uriFile))

        dealResult(imageFile.absolutePath, uriFile)
    }

    override fun onImageSelectList(imageList: MutableList<MediaVo>) {
        updateDoneText(imageList.size)
    }

    override fun onDestroy() {
        MediaConstant.clear()
        super.onDestroy()
    }

    private fun dealResult(path: String? = null, uriFile: Uri? = null) {
        val allResultList = arrayListOf<String>()
        val allUriResultList = arrayListOf<Uri>()
        MediaConstant.getSelectMediaList().forEach {
            if (it.path != null) {
                allResultList.add(it.path)
            }
            if (it.uri != null) {
                allUriResultList.add(it.uri)
            }
        }
        if (uriFile != null) allUriResultList.add(uriFile)
        if (path != null) allResultList.add(path)

        setIntentResult(allResultList, allUriResultList)
    }

    private fun setIntentResult(pathList: ArrayList<String>, uriList: ArrayList<Uri>) {
        val data = Intent()
        data.putStringArrayListExtra(MediaConstant.KEY_EXTRA_RESULT, pathList)
        data.putParcelableArrayListExtra(MediaConstant.KEY_EXTRA_RESULT_URI, uriList)
        setResult(RESULT_OK, data)
        finish()
    }
}