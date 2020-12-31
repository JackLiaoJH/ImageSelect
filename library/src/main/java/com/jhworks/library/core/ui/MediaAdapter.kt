package com.jhworks.library.core.ui

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.recyclerview.widget.RecyclerView
import com.jhworks.library.ImageSelector
import com.jhworks.library.R
import com.jhworks.library.core.MediaConstant
import com.jhworks.library.core.vo.MediaConfigVo
import com.jhworks.library.core.vo.MediaType
import com.jhworks.library.core.vo.MediaUiConfigVo
import com.jhworks.library.core.vo.MediaVo
import com.jhworks.library.utils.SlScreenUtils
import java.io.File

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/6/11 11:31
 */
class MediaAdapter(context: Context,
                   private var showCamera: Boolean,
                   private val column: Int,
                   private val mediaConfig: MediaConfigVo?)
    : RecyclerView.Adapter<MediaHolder>() {
    companion object {
        private const val TYPE_CAMERA = 0
        private const val TYPE_NORMAL = 1
    }

    private var mImages = arrayListOf<MediaVo>()

    var mOnItemClickListener: OnItemClickListener? = null
    private val mInflater = LayoutInflater.from(context)
    var mGridWidth = 0
    private var mSpaceSize = 0
    private var mLayoutParams: FrameLayout.LayoutParams
    var showSelectIndicator = true

    var placeholderResId = mediaConfig?.placeholderResId ?: R.drawable.ic_sl_image_default
    var errorResId = mediaConfig?.errorResId ?: R.drawable.ic_sl_image_default
    private val mMaxSelectCount = mediaConfig?.maxCount ?: MediaConstant.DEFAULT_IMAGE_SIZE

    init {
        val screenWidth = SlScreenUtils.getScreenWidth(context)
        mSpaceSize = context.resources.getDimensionPixelSize(R.dimen.sl_space_size)
        mGridWidth = (screenWidth - mSpaceSize * (column - 1)) / column
        mLayoutParams = FrameLayout.LayoutParams(mGridWidth, mGridWidth)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaHolder {
        return if (viewType == TYPE_CAMERA) {
            MediaHolder(mInflater.inflate(R.layout.sl_list_item_camera, parent, false), this, mMaxSelectCount, mediaConfig)
        } else {
            MediaHolder(mInflater.inflate(R.layout.sl_list_item_image, parent, false), this, mMaxSelectCount, mediaConfig)
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
        val itemLp = holder.itemView.layoutParams as? RecyclerView.LayoutParams
        itemLp?.width = mGridWidth
        itemLp?.height = mGridWidth

        itemLp?.topMargin = mSpaceSize
        when {
            (position + 1) % column == 0 -> {
                // end
                itemLp?.rightMargin = 0
                itemLp?.leftMargin = mSpaceSize
            }
            position % column == 0 -> {
                // first
                itemLp?.rightMargin = mSpaceSize
                itemLp?.leftMargin = 0
            }
            else -> {
                itemLp?.rightMargin = mSpaceSize
                itemLp?.leftMargin = mSpaceSize / 2
            }
        }


        val imgLp = holder.image?.layoutParams as? FrameLayout.LayoutParams
        if (showCamera) {
            if (position != 0) {
                imgLp?.width = mGridWidth
                imgLp?.height = mGridWidth
            }
        } else {
            imgLp?.width = mGridWidth
            imgLp?.height = mGridWidth
        }
        holder.bindData(data, position)
        holder.itemView.setOnClickListener {
            mOnItemClickListener?.onItemClick(data, position)
        }
    }

    fun getItem(i: Int): MediaVo? {
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
}

class MediaHolder(itemView: View, private val adapter: MediaAdapter, private val maxCount: Int,
                  private val mediaConfig: MediaConfigVo?)
    : RecyclerView.ViewHolder(itemView) {
    val image: ImageView? = itemView.findViewById(R.id.sl_image)
    private val mCheckBox: AppCompatCheckBox? = itemView.findViewById(R.id.sl_checkmark)
    private val view: View? = itemView.findViewById(R.id.sl_check_container)
    private val ivVideoIcon: ImageView? = itemView.findViewById(R.id.sl_video_icon)
    private val tvVideoTime: TextView? = itemView.findViewById(R.id.tv_video_time)

    init {
        itemView.tag = this
    }

    fun bindData(data: MediaVo?, position: Int) {
        if (data == null) return
        // 处理单选和多选状态
        if (adapter.showSelectIndicator) {
            mCheckBox?.visibility = View.VISIBLE
            view?.setOnClickListener { mCheckBox?.performClick() }
            mCheckBox?.setOnClickListener {
                if (MediaConstant.getSelectMediaList().size >= maxCount) {
                    mCheckBox.isChecked = false
                }
                adapter.mOnItemClickListener?.onCheckClick(data, position)
            }
            mCheckBox?.isChecked = data.isSelect
        } else {
            mCheckBox?.visibility = View.GONE
        }

        if (mediaConfig?.mediaType == MediaType.VIDEO) {
            ivVideoIcon?.visibility = View.VISIBLE
            tvVideoTime?.visibility = View.VISIBLE
            tvVideoTime?.text = formatDuration((data.duration + 500) / 1000)
        } else {
            ivVideoIcon?.visibility = View.GONE
            tvVideoTime?.visibility = View.GONE
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

    /**
     * 格式化时长在一个小时以内的时间
     */
    private fun formatDuration(duration: Int): String {
        if (duration < 10) return "00:0$duration"
        if (duration < 60) return "00:$duration"
        val second = duration % 60
        val min = duration / 60
        if (min < 10) {
            return if (second < 10) "0${min}:0${second}" else "0${min}:${second}"
        }
        return if (second < 10) "${min}:0${second}" else "${min}:${second}"
    }
}

interface OnItemClickListener {
    fun onItemClick(media: MediaVo?, position: Int)
    fun onCheckClick(media: MediaVo?, position: Int)
}