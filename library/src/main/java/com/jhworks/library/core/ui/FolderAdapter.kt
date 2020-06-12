package com.jhworks.library.core.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.jhworks.library.ImageSelector
import com.jhworks.library.R
import com.jhworks.library.core.vo.FolderVo
import com.jhworks.library.core.vo.MediaConfigVo
import com.jhworks.library.core.vo.MediaType
import com.jhworks.library.core.vo.MediaUiConfigVo

/**
 * 文件夹Adapter
 */
class FolderAdapter(private val mContext: Context,
                    private val mediaConfig: MediaConfigVo?)
    : BaseAdapter() {
    private val mInflater = LayoutInflater.from(mContext)
    private var mFolders = arrayListOf<FolderVo>()
    var mImageSize = mContext.resources.getDimensionPixelOffset(R.dimen.mis_folder_cover_size)
    private var lastSelected = 0
    private val folderSize = mContext.resources.getDimensionPixelSize(R.dimen.mis_folder_cover_size)
    var placeholderResId = mediaConfig?.placeholderResId ?: R.drawable.ic_image_default
    var errorResId = mediaConfig?.errorResId ?: R.drawable.ic_image_default

    /**
     * 设置数据集
     *
     * @param folders -
     */
    fun setData(folders: MutableList<FolderVo>) {
        mFolders.clear()
        if (folders.isNotEmpty()) {
            mFolders.addAll(folders)
        }
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return mFolders.size + 1
    }

    override fun getItem(i: Int): FolderVo? {
        return if (i == 0) null else mFolders[i - 1]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(i: Int, contentView: View?, viewGroup: ViewGroup): View {
        var view = contentView
        val holder: ViewHolder?
        if (view == null) {
            view = mInflater.inflate(R.layout.mis_list_item_folder, viewGroup, false)
            holder = ViewHolder(view)
        } else {
            holder = view.tag as? ViewHolder
        }
        if (holder != null) {
            if (i == 0) {
                holder.name.setText(
                        if (mediaConfig?.mediaType == MediaType.IMAGE)
                            R.string.mis_folder_image_all
                        else
                            R.string.mis_folder_video_all)
                holder.path.setText(R.string.sd_card)
                holder.size.text = mContext.resources.getString(R.string.mis_photo_unit, totalImageSize)
                if (mFolders.size > 0) {
                    val f = mFolders[0]
                    ImageSelector.getImageEngine().loadImage(holder.cover,
                            MediaUiConfigVo(f.cover?.path, folderSize, folderSize,
                                    placeholderResId, errorResId))
                }
            } else {
                holder.bindData(getItem(i))
            }
            if (lastSelected == i) {
                holder.indicator.visibility = View.VISIBLE
            } else {
                holder.indicator.visibility = View.INVISIBLE
            }
        }
        return view!!
    }

    private val totalImageSize: Int
        get() {
            var result = 0
            if (mFolders.isNotEmpty()) {
                for (f in mFolders) {
                    result += f.mediaStoreList?.size ?: 0
                }
            }
            return result
        }

    var selectIndex: Int
        get() = lastSelected
        set(i) {
            if (lastSelected == i) return
            lastSelected = i
            notifyDataSetChanged()
        }

    internal inner class ViewHolder(view: View) {
        var cover: ImageView = view.findViewById(R.id.cover)
        var name: TextView = view.findViewById(R.id.name)
        var path: TextView = view.findViewById(R.id.path)
        var size: TextView = view.findViewById(R.id.size)
        var indicator: ImageView = view.findViewById(R.id.indicator)

        fun bindData(data: FolderVo?) {
            data ?: return

            name.text = data.name
            path.text = data.path
            if (data.mediaStoreList != null) {
                size.text = mContext.resources.getString(R.string.mis_photo_unit,
                        data.mediaStoreList!!.size)
            } else {
                size.text = mContext.resources.getString(R.string.mis_no_photo_unit)
            }
            if (data.cover != null) {
                ImageSelector.getImageEngine().loadImage(cover,
                        MediaUiConfigVo(data.cover?.path, mImageSize, mImageSize,
                                placeholderResId, errorResId))
            } else {
                cover.setImageResource(placeholderResId)
            }
        }

        init {
            view.tag = this
        }
    }
}