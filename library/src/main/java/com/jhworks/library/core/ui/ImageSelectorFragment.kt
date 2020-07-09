package com.jhworks.library.core.ui

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.ListPopupWindow
import androidx.loader.app.LoaderManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.jhworks.library.R
import com.jhworks.library.core.MediaConstant
import com.jhworks.library.core.vo.FolderVo
import com.jhworks.library.core.vo.MediaType
import com.jhworks.library.core.vo.MediaVo
import com.jhworks.library.core.vo.SelectMode
import com.jhworks.library.decoration.DividerGridItemDecoration
import com.jhworks.library.utils.SlFileUtils
import com.jhworks.library.utils.SlScreenUtils
import java.io.File
import java.io.IOException

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/6/11 11:04
 */
class ImageSelectorFragment : MediaLoaderFragment() {
    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 110
        private const val REQUEST_CAMERA = 100
        const val REQUEST_IMAGE_VIEW = 120

        private const val KEY_TEMP_FILE = "key_temp_file"
    }


    private var mCallback: Callback? = null

    private lateinit var mMediaAdapter: MediaAdapter
    private lateinit var mFolderAdapter: FolderAdapter

    private var mFolderPopupWindow: ListPopupWindow? = null

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mCategoryText: TextView
    private lateinit var mPopupAnchorView: View

    private var mTmpFile: File? = null

    private var hasFolderGened = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCallback = try {
            activity as? Callback
        } catch (e: ClassCastException) {
            throw ClassCastException("The Activity must implement ImageSelectorFragment.Callback interface...")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(KEY_TEMP_FILE, mTmpFile)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            mTmpFile = savedInstanceState.getSerializable(KEY_TEMP_FILE) as? File
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sl_fragment_multi_image, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // load image data
        LoaderManager.getInstance(this).initLoader(R.id.loader_all_media_store_data, null, this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isOpenCameraOnly()) {
            showCameraAction()
            return
        }

        val selectMode = selectMode()
        val imageSpanCount = imageSpanCount()
        mMediaAdapter = MediaAdapter(context!!, showCamera(), imageSpanCount, mMediaConfig)
        mMediaAdapter.showSelectIndicator = selectMode == SelectMode.MODE_MULTI
        mRecyclerView = view.findViewById(R.id.sl_grid)
        val layoutManager = GridLayoutManager(context, imageSpanCount)
        mRecyclerView.layoutManager = layoutManager
        mRecyclerView.addItemDecoration(DividerGridItemDecoration(context!!, R.drawable.sl_divider))
        closeDefaultAnimator()
        mRecyclerView.adapter = mMediaAdapter

        mMediaAdapter.mOnItemClickListener = object : OnItemClickListener {
            override fun onItemClick(media: MediaVo?, position: Int) {
                if (mMediaAdapter.isShowCamera()) {
                    if (position == 0) {
                        if (selectMode == SelectMode.MODE_SINGLE) {
                            showCameraAction()
                            return
                        }
                        val selectList = MediaConstant.getSelectMediaList()
                        if (selectImageMaxCount() <= selectList.size) {
                            Toast.makeText(activity, R.string.sl_msg_amount_limit, Toast.LENGTH_SHORT).show()
                            return
                        }
                        showCameraAction()
                    } else {
                        openImageActivity(position - 1, selectMode, media?.path)
                    }
                } else {
                    openImageActivity(position, selectMode, media?.path)
                }
            }

            override fun onCheckClick(media: MediaVo?, position: Int) {
                media ?: return
                val selectList = MediaConstant.getSelectMediaList()
                if (mMediaAdapter.isShowCamera()) {
                    if (position == 0) {
                        if (selectImageMaxCount() <= selectList.size) {
                            Toast.makeText(activity, R.string.sl_msg_amount_limit, Toast.LENGTH_SHORT).show()
                            return
                        }
                        showCameraAction()
                    } else {
                        selectImageFromGrid(media, selectMode, position)
                    }
                } else {
                    selectImageFromGrid(media, selectMode, position)
                }
            }
        }

        mPopupAnchorView = view.findViewById(R.id.sl_footer)
        mCategoryText = view.findViewById(R.id.sl_category_btn)
        setFolderName()
        mCategoryText.setOnClickListener { showFolderPopWin() }

        mFolderAdapter = FolderAdapter(context!!, mMediaConfig)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (mFolderPopupWindow != null) {
            if (mFolderPopupWindow!!.isShowing) {
                mFolderPopupWindow!!.dismiss()
            }
        }
        super.onConfigurationChanged(newConfig)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                if (mTmpFile != null) {
                    mCallback?.onCameraShot(mTmpFile)
                }
            } else {
                // delete tmp file
                while (mTmpFile != null && mTmpFile!!.exists()) {
                    val success = mTmpFile!!.delete()
                    if (success) {
                        mTmpFile = null
                    }
                }
                if (showCamera()) {
                    activity?.finish()
                }
            }
        } else if (requestCode == REQUEST_IMAGE_VIEW) {
            if (resultCode == Activity.RESULT_OK) {
                val selectList = MediaConstant.getSelectMediaList()
                if (selectList.isNotEmpty()) {
                    updateSelectMedia()
                    mCallback?.onImageSelectList(selectList)
                }
            }
        }
    }

    override fun onLoadFinishedUpdateUi(data: MutableList<MediaVo>?) {
        if (!hasFolderGened) {
            mFolderAdapter.setData(mResultFolder)
            hasFolderGened = true
        }
        if (isRestartLoaded) {
            isRestartLoaded = false
            mMediaAdapter.setData(data)
        }
        updateSelectMedia()
        mMediaAdapter.notifyDataSetChanged()
    }

    private fun closeDefaultAnimator() {
        mRecyclerView.itemAnimator?.addDuration = 0
        mRecyclerView.itemAnimator?.changeDuration = 0
        mRecyclerView.itemAnimator?.moveDuration = 0
        mRecyclerView.itemAnimator?.removeDuration = 0
        (mRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    private fun setFolderName() {
        mCategoryText.setText(
                if (mMediaConfig?.mediaType == MediaType.IMAGE)
                    R.string.sl_folder_image_all
                else
                    R.string.sl_folder_video_all)
    }

    private fun openImageActivity(position: Int, mode: Int, path: String?) {
        if (mode == SelectMode.MODE_MULTI) {
            val intent = Intent(context, ImageDetailActivity::class.java)
            intent.putExtra(MediaConstant.KEY_EXTRA_CURRENT_POSITION, position)
            intent.putExtra(MediaConstant.KEY_EXTRA_SELECT_COUNT, selectImageMaxCount())
            startActivityForResult(intent, REQUEST_IMAGE_VIEW)
        } else if (mode == SelectMode.MODE_SINGLE) {
            mCallback?.onSingleImageSelected(path)
        }
    }

    private fun selectImageFromGrid(media: MediaVo, mode: Int, position: Int) {
        if (mode == SelectMode.MODE_MULTI) {
            val selectList = MediaConstant.getSelectMediaList()
            if (selectList.contains(media) && media.isSelect) {
                selectList.remove(media)
                mCallback?.onImageUnselected(media.path)
                media.isSelect = false

                // remove origin data
                mMediaConfig?.originData?.let {
                    if (it.contains(media.path)) it.remove(media.path)
                }
            } else {
                if (selectImageMaxCount() <= selectList.size) {
                    Toast.makeText(activity, R.string.sl_msg_amount_limit, Toast.LENGTH_SHORT).show()
                    return
                }
                selectList.add(media)
                mCallback?.onImageSelected(media.path)
                media.isSelect = true
            }
            mMediaAdapter.notifyItemChanged(position)
        } else if (mode == SelectMode.MODE_SINGLE) {
            mCallback?.onSingleImageSelected(media.path)
        }
    }

    private fun showFolderPopWin() {
        if (mFolderPopupWindow == null) {
            createPopupFolderList()
        }
        if (mFolderPopupWindow != null && mFolderPopupWindow!!.isShowing) {
            mFolderPopupWindow?.dismiss()
        } else {
            mFolderPopupWindow?.show()
            var index = mFolderAdapter.selectIndex
            index = if (index == 0) index else index - 1
            if (null != mFolderPopupWindow && mFolderPopupWindow?.listView != null)
                mFolderPopupWindow?.listView?.setSelection(index)
        }
    }

    /**
     * Create popup ListView
     */
    private fun createPopupFolderList() {
        val point = SlScreenUtils.getScreenSize(context!!)
        val width = point.x
        val height = (point.y * (4.5f / 8.0f)).toInt()
        mFolderPopupWindow = ListPopupWindow(activity!!)
        mFolderPopupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        mFolderPopupWindow?.setAdapter(mFolderAdapter)
        mFolderPopupWindow?.setContentWidth(width)
        mFolderPopupWindow?.width = width
        mFolderPopupWindow?.height = height
        mFolderPopupWindow?.anchorView = mPopupAnchorView
        mFolderPopupWindow?.isModal = true
        mFolderPopupWindow?.setOnItemClickListener { adapterView, _, i, _ ->
            mFolderAdapter.selectIndex = i

            mRecyclerView.postDelayed({
                mFolderPopupWindow?.dismiss()
                if (i == 0) {
                    isRestartLoaded = true
                    LoaderManager.getInstance(this)
                            .restartLoader(R.id.loader_all_media_store_data, null, this)
                    setFolderName()
                    mMediaAdapter.setShowCamera(showCamera())
                } else {
                    val folder = adapterView.adapter.getItem(i) as? FolderVo
                    if (null != folder) {
                        setAllMediaList(folder.mediaStoreList)

                        updateSelectMedia()
                        mMediaAdapter.setData(MediaConstant.getAllMediaList())
                        mCategoryText.text = folder.name
                    }
                    mMediaAdapter.setShowCamera(false)
                }
                mRecyclerView.smoothScrollToPosition(0)
            }, 100)
        }
    }

    private fun updateSelectMedia() {
        val selectList = MediaConstant.getSelectMediaList()
        val allSelectList = MediaConstant.getAllMediaList()
        selectList.forEach { selectAt ->
            run breaking@{
                allSelectList.forEach {
                    if (it.path == selectAt.path) {
                        it.isSelect = true
                        return@breaking
                    }
                }
            }
        }
    }

    /**
     * Open camera
     */
    private fun showCameraAction() {
        if (checkPermission(Manifest.permission.CAMERA)) {
            requestPermission(Manifest.permission.CAMERA,
                    getString(R.string.sl_permission_camera), REQUEST_CAMERA_PERMISSION)
        } else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(activity!!.packageManager) != null) {
                try {
                    mTmpFile = SlFileUtils.createTmpFile(activity!!)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                if (mTmpFile != null && mTmpFile!!.exists()) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile))
                    } else {
                        val contentValues = ContentValues(1)
                        contentValues.put(MediaStore.Images.Media.DATA, mTmpFile?.absolutePath)
                        val uri = createImageUri(contentValues)
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                    }
                    startActivityForResult(intent, REQUEST_CAMERA)
                } else {
                    Toast.makeText(activity, R.string.sl_error_image_not_exist, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(activity, R.string.sl_msg_no_camera, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createImageUri(contentValues: ContentValues): Uri? {
        val status: String = Environment.getExternalStorageState()
        return if (status == Environment.MEDIA_MOUNTED) {
            context?.let { context?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) }
        } else {
            context?.let { context?.contentResolver?.insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, contentValues) }
        }
    }

    private fun requestPermission(permission: String, rationale: String, requestCode: Int) {
        if (shouldShowRequestPermissionRationale(permission)) {
            AlertDialog.Builder(context!!)
                    .setTitle(R.string.sl_permission_dialog_title)
                    .setMessage(rationale)
                    .setPositiveButton(R.string.sl_permission_dialog_ok) { _, _ ->
                        requestPermissions(arrayOf(permission), requestCode)
                    }.setNegativeButton(R.string.sl_permission_dialog_cancel, null)
                    .create()
                    .show()
        } else {
            requestPermissions(arrayOf(permission), requestCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showCameraAction()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        }
    }

    /**
     * Callback for host activity
     */
    interface Callback {
        fun onSingleImageSelected(path: String?)
        fun onImageSelected(path: String?)
        fun onImageUnselected(path: String?)
        fun onCameraShot(imageFile: File?)
        fun onImageSelectList(imageList: MutableList<MediaVo>)
    }
}