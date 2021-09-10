package com.jhworks.library.core.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.viewpager.widget.PagerAdapter
import com.jhworks.library.ImageSelector
import com.jhworks.library.core.vo.ImageInfoVo
import com.jhworks.library.core.vo.MediaUiConfigVo
import com.jhworks.library.utils.SlScreenUtils
import com.jhworks.library.view.SlLongImageView
import com.jhworks.library.view.SlPhotoView

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2021/9/9 17:49
 */
class ImagePagerAdapter(
    private val mContext: Context,
    private val imageList: MutableList<ImageInfoVo> = ArrayList()
) : PagerAdapter() {

    private val screenW = SlScreenUtils.getScreenWidth(mContext)

    override fun getCount(): Int = imageList.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageInfo = imageList[position]
        val photoView = SlPhotoView(mContext, position, screenW, imageInfo)

        val uiConfig = MediaUiConfigVo(null, imageInfoVo = imageInfo)
        ImageSelector.getImageEngine()
            .loadBigImage(photoView, uiConfig, photoView.getProgressListener())
        container.addView(
            photoView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return photoView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val view: View? = (`object` as FrameLayout).findViewWithTag(position)
        if (view is SlLongImageView) {
            view.recycle()
        }
        container.removeView(`object` as View)
    }

}