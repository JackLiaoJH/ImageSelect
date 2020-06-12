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
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import androidx.loader.app.LoaderManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jhworks.library.R
import com.jhworks.library.core.MediaConstant
import com.jhworks.library.core.vo.FolderVo
import com.jhworks.library.core.vo.MediaType
import com.jhworks.library.core.vo.MediaVo
import com.jhworks.library.core.vo.SelectMode
import com.jhworks.library.decoration.DividerGridItemDecoration
import com.jhworks.library.utils.FileUtils
import com.jhworks.library.utils.ScreenUtils
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
        private const val REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 110
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

    // image result data set
    private var mResultList = arrayListOf<String>()
    private var hasFolderGened = false
    private var isRestartLoaded = true

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val selectMode = selectMode()
        if (selectMode == SelectMode.MODE_MULTI
                && mMediaConfig != null
                && mMediaConfig!!.originData != null) {
            if (mResultList.isNotEmpty()) mResultList.clear()
            mResultList.addAll(mMediaConfig!!.originData!!)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.mis_fragment_multi_image, container, false)
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
        mRecyclerView = view.findViewById(R.id.grid)
        val layoutManager = GridLayoutManager(context, imageSpanCount)
        mRecyclerView.layoutManager = layoutManager
        mRecyclerView.addItemDecoration(DividerGridItemDecoration(context!!, R.drawable.divider))
        mRecyclerView.adapter = mMediaAdapter
        mMediaAdapter.mOnItemClickListener = object : OnItemClickListener {
            override fun onItemClick(media: MediaVo?, position: Int) {
                if (mMediaAdapter.isShowCamera()) {
                    if (position == 0) {
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
                if (mMediaAdapter.isShowCamera()) {
                    if (position == 0) {
                        showCameraAction()
                    } else {
                        selectImageFromGrid(media, selectMode)
                    }
                } else {
                    selectImageFromGrid(media, selectMode)
                }
            }
        }

        mPopupAnchorView = view.findViewById(R.id.footer)
        mCategoryText = view.findViewById(R.id.category_btn)
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
                if (data != null) {
                    if (mResultList.isNotEmpty()) mResultList.clear()
                    val selectList = data.getStringArrayListExtra(MediaConstant.KEY_EXTRA_IMAGE_SELECT_LIST)
                    if (selectList != null) mResultList.addAll(selectList)

                    if (mResultList.isNotEmpty()) {
                        mMediaAdapter.setDefaultSelected(mResultList)
                        mCallback?.onImageSelectList(mResultList)
                    }
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
            mMediaAdapter.setData(data)
        }
        if (mResultList.isNotEmpty()) {
            mMediaAdapter.setDefaultSelected(mResultList)
        }
    }

    private fun setFolderName() {
        mCategoryText.setText(
                if (mMediaConfig?.mediaType == MediaType.IMAGE)
                    R.string.mis_folder_image_all
                else
                    R.string.mis_folder_video_all)
    }

    private fun openImageActivity(position: Int, mode: Int, path: String?) {
        if (mode == SelectMode.MODE_MULTI) {
            mResultList.forEach o@{
                if (TextUtils.isEmpty(it)) return@o
                for (media in mAllMediaList) {
                    if (it == media.path) {
                        media.isSelect = true
                    }
                }
            }
            val intent = Intent(context, ImageDetailActivity::class.java)
            intent.putParcelableArrayListExtra(MediaConstant.KEY_EXTRA_ALL_IMAGE_LIST, mAllMediaList)
            intent.putExtra(MediaConstant.KEY_EXTRA_CURRENT_POSITION, position)
            intent.putExtra(MediaConstant.KEY_EXTRA_SELECT_COUNT, selectImageCount())
            startActivityForResult(intent, REQUEST_IMAGE_VIEW)
        } else if (mode == SelectMode.MODE_SINGLE) {
            path ?: return
            mCallback?.onSingleImageSelected(path)
        }
    }

    private fun selectImageFromGrid(media: MediaVo, mode: Int) {
        if (mode == SelectMode.MODE_MULTI) {
            if (mResultList.contains(media.path)) {
                mResultList.remove(media.path)
                mCallback?.onImageUnselected(media.path)
            } else {
                if (selectImageCount() == mResultList.size) {
                    Toast.makeText(activity, R.string.mis_msg_amount_limit, Toast.LENGTH_SHORT).show()
                    return
                }
                mResultList.add(media.path!!)
                mCallback?.onImageSelected(media.path)
            }
            mMediaAdapter.select(media)
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
        val point = ScreenUtils.getScreenSize(context!!)
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
                    isRestartLoaded = false
                    val folder = adapterView.adapter.getItem(i) as? FolderVo
                    if (null != folder) {
                        if (mAllMediaList.size > 0) mAllMediaList.clear()
                        if (folder.mediaStoreList != null) {
                            mAllMediaList.addAll(folder.mediaStoreList!!)
                        }
                        mMediaAdapter.setData(folder.mediaStoreList)
                        mCategoryText.text = folder.name
                        if (mResultList.size > 0) {
                            mMediaAdapter.setDefaultSelected(mResultList)
                        }
                    }
                    mMediaAdapter.setShowCamera(false)
                }
                mRecyclerView.smoothScrollToPosition(0)
            }, 100)
        }
    }

    /**
     * Open camera
     */
    private fun showCameraAction() {
        if (ContextCompat.checkSelfPermission(context!!,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    getString(R.string.mis_permission_rationale_write_storage),
                    REQUEST_STORAGE_WRITE_ACCESS_PERMISSION)
        } else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(activity!!.packageManager) != null) {
                try {
                    mTmpFile = FileUtils.createTmpFile(activity!!)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                if (mTmpFile != null && mTmpFile!!.exists()) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile))
                    } else {
                        val contentValues = ContentValues(1)
                        contentValues.put(MediaStore.Images.Media.DATA, mTmpFile?.absolutePath)
                        val uri = context?.contentResolver?.insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                    }
                    startActivityForResult(intent, REQUEST_CAMERA)
                } else {
                    Toast.makeText(activity, R.string.mis_error_image_not_exist, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(activity, R.string.mis_msg_no_camera, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestPermission(permission: String, rationale: String, requestCode: Int) {
        if (shouldShowRequestPermissionRationale(permission)) {
            AlertDialog.Builder(context!!)
                    .setTitle(R.string.mis_permission_dialog_title)
                    .setMessage(rationale)
                    .setPositiveButton(R.string.mis_permission_dialog_ok) { _, _ ->
                        requestPermissions(arrayOf(permission), requestCode)
                    }.setNegativeButton(R.string.mis_permission_dialog_cancel, null)
                    .create()
                    .show()
        } else {
            requestPermissions(arrayOf(permission), requestCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_STORAGE_WRITE_ACCESS_PERMISSION) {
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
        fun onImageSelectList(imageList: MutableList<String>?)
    }
}