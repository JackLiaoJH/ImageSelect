package com.jhworks.library.core.ui

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.recyclerview.widget.RecyclerView
import com.jhworks.library.ImageSelector
import com.jhworks.library.R
import com.jhworks.library.core.vo.MediaConfigVo
import com.jhworks.library.core.vo.MediaUiConfigVo
import com.jhworks.library.core.vo.MediaVo
import com.jhworks.library.utils.ScreenUtils
import java.io.File

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/6/11 11:31
 */
class MediaAdapter(context: Context,
                   private var showCamera: Boolean,
                   column: Int,
                   mediaConfig: MediaConfigVo?)
    : RecyclerView.Adapter<MediaHolder>() {
    companion object {
        private const val TYPE_CAMERA = 0
        private const val TYPE_NORMAL = 1
    }

    val mSelectedImages = arrayListOf<MediaVo>()
    private var mImages = arrayListOf<MediaVo>()

    var mOnItemClickListener: OnItemClickListener? = null
    private val mInflater = LayoutInflater.from(context)
    var mGridWidth = 0
    private var mLayoutParams: FrameLayout.LayoutParams
    var showSelectIndicator = true

    var placeholderResId = mediaConfig?.placeholderResId ?: R.drawable.ic_image_default
    var errorResId = mediaConfig?.errorResId ?: R.drawable.ic_image_default

    init {
        val screenSize = ScreenUtils.getScreenSize(context)
        val width = screenSize.x
        val mSpaceSize = context.resources.getDimensionPixelSize(R.dimen.mis_space_size)
        mGridWidth = (width - mSpaceSize * (2 + column - 1)) / column
        mLayoutParams = FrameLayout.LayoutParams(mGridWidth, mGridWidth)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaHolder {
        return if (viewType == TYPE_CAMERA) {
            MediaHolder(mInflater.inflate(R.layout.mis_list_item_camera, parent, false), this)
        } else {
            MediaHolder(mInflater.inflate(R.layout.mis_list_item_image, parent, false), this)
        }
    }

    override fun getItemCount(): Int {
        return if (showCamera) mImages.size + 1 else mImages.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (showCamera) {
            if (position == 0) TYPE_CAMERA else TYPE_NORMAL
        } else TYPE_NORMAL
    }

    override fun onBindViewHolder(holder: MediaHolder, position: Int) {
        val data = getItem(position)
        if (showCamera) {
            if (position != 0) {
                holder.image?.layoutParams = mLayoutParams
            } else {
                holder.itemView.layoutParams = mLayoutParams
            }
        } else {
            holder.image?.layoutParams = mLayoutParams
        }
        holder.bindData(data, position)
        holder.itemView.setOnClickListener {
            mOnItemClickListener?.onItemClick(data, position)
        }
    }

    private fun getItem(i: Int): MediaVo? {
        return if (showCamera) {
            if (i == 0) null else mImages[i - 1]
        } else {
            mImages[i]
        }
    }

    /**
     * 设置数据集
     *
     * @param images -
     */
    fun setData(images: MutableList<MediaVo>?) {
        mSelectedImages.clear()
        mImages.clear()
        if (images != null && images.size > 0) {
            mImages.addAll(images)
        }
        notifyDataSetChanged()
    }

    fun setShowCamera(b: Boolean) {
        if (showCamera == b) return
        showCamera = b
        notifyDataSetChanged()
    }

    fun isShowCamera(): Boolean {
        return showCamera
    }

    /**
     * 选择某个图片，改变选择状态
     *
     * @param media -
     */
    fun select(media: MediaVo) {
        if (mSelectedImages.contains(media)) {
            mSelectedImages.remove(media)
        } else {
            mSelectedImages.add(media)
        }
        notifyDataSetChanged()
    }

    /**
     * 通过图片路径设置默认选择
     *
     * @param resultList -
     */
    fun setDefaultSelected(resultList: MutableList<String>) {
        var media: MediaVo?
        for (path in resultList) {
            media = getImageByPath(path)
            if (media != null) {
                mSelectedImages.add(media)
            }
        }
        if (mSelectedImages.size > 0) {
            notifyDataSetChanged()
        }
    }

    private fun getImageByPath(path: String): MediaVo? {
        if (mImages.size > 0) {
            for (media in mImages) {
                if (media.path != null && media.path.equals(path, ignoreCase = true)) {
                    return media
                }
            }
        }
        return null
    }
}

class MediaHolder(itemView: View, private val adapter: MediaAdapter)
    : RecyclerView.ViewHolder(itemView) {
    var image: ImageView? = itemView.findViewById(R.id.image)
    private var mCheckBox: AppCompatCheckBox? = itemView.findViewById(R.id.checkmark)

    init {
        itemView.tag = this
    }

    fun bindData(data: MediaVo?, position: Int) {
        if (data == null) return
        // 处理单选和多选状态
        if (adapter.showSelectIndicator) {
            mCheckBox?.visibility = View.VISIBLE
            mCheckBox?.setOnClickListener {
                adapter.mOnItemClickListener?.onCheckClick(data, position)
            }
            mCheckBox?.setButtonDrawable(
                    if (adapter.mSelectedImages.contains(data))
                        R.drawable.ic_select_pressed
                    else
                        R.drawable.ic_select_normal)
        } else {
            mCheckBox?.visibility = View.GONE
        }
        if (TextUtils.isEmpty(data.path)) {
            image?.setImageResource(adapter.placeholderResId)
            return
        }
        val imageFile = File(data.path!!)
        if (imageFile.exists()) {
            ImageSelector.getImageEngine().loadImage(image!!,
                    MediaUiConfigVo(data.path, adapter.mGridWidth,
                            adapter.mGridWidth, adapter.placeholderResId,
                            adapter.errorResId))
        } else {
            image?.setImageResource(adapter.placeholderResId)
        }
    }
}

interface OnItemClickListener {
    fun onItemClick(media: MediaVo?, position: Int)
    fun onCheckClick(media: MediaVo?, position: Int)
}