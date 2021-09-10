package com.jhworks.library.core.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.jhworks.library.R
import com.jhworks.library.core.MediaConstant
import com.jhworks.library.core.ui.adapter.ImagePagerAdapter
import com.jhworks.library.core.vo.ImageInfoVo
import com.jhworks.library.utils.CheckNullUtils
import com.jhworks.library.view.ScrollDownPhotoView

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2021/9/9 17:07
 */
class ImagePageActivity : ImageBaseActivity(), ViewPager.OnPageChangeListener {

    companion object {
        /**
         *
         *
         * @param activity          -
         * @param position       -
         * @param imgList -
         */
        fun start(activity: Activity, position: Int, imgList: ArrayList<ImageInfoVo>) {
            if (activity.isFinishing) return
            activity.startActivity(createIntent(activity, imgList, position))
        }

        private fun createIntent(
            context: Context,
            imgList: ArrayList<ImageInfoVo>,
            position: Int
        ): Intent {
            val intent = Intent(context, ImagePageActivity::class.java)
            intent.putExtra(MediaConstant.KEY_EXTRA_CURRENT_POSITION, position)
            intent.putParcelableArrayListExtra(MediaConstant.KEY_BIG_IMAGE_LIST, imgList)
            return intent
        }
    }

    private lateinit var scrollDownPhotoView: ScrollDownPhotoView
    private lateinit var viewPager: ViewPager

    //    private lateinit var indicator: CircleIndicator
    private lateinit var tvPageNum: TextView

    private var mCurrentPosition = 0
    private var bigImageList: MutableList<ImageInfoVo>? = null

    override fun setLayout(): Int = R.layout.activity_sl_image_pager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarColor(R.color.sl_transparent, true)

        scrollDownPhotoView = findViewById(R.id.sl_scroll_down_photo_view)
        viewPager = findViewById(R.id.sl_page_image_viewpager)
//        indicator = findViewById(R.id.indicator)
        tvPageNum = findViewById(R.id.sl_page_image_num)

        bigImageList = intent.getParcelableArrayListExtra(MediaConstant.KEY_BIG_IMAGE_LIST)
        if (CheckNullUtils.isListEmpty(bigImageList)) {
            finish()
            return
        }
        mCurrentPosition = intent.getIntExtra(MediaConstant.KEY_EXTRA_CURRENT_POSITION, 0)

        updatePageNum(mCurrentPosition)

        val adapter = ImagePagerAdapter(this, bigImageList!!)
        viewPager.adapter = adapter
        viewPager.currentItem = mCurrentPosition
        viewPager.addOnPageChangeListener(this)

        scrollDownPhotoView.setOpenDownAnimate(true)
        scrollDownPhotoView.setOnViewTouchListener(object :
            ScrollDownPhotoView.OnViewTouchListener {
            override fun onFinish() {
//                indicator.visibility = GONE
                finish()
                overridePendingTransition(0, 0)
            }

            override fun onPreFinish() {
//                indicator.visibility = GONE
            }

            override fun onMoving(deltaX: Float, deltaY: Float) {

            }
        })
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        mCurrentPosition = position
        updatePageNum(mCurrentPosition)
    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    private fun setIndicatorVisibility(visibility: Boolean) {
//        if (photoPagerBean.getBigImgUrls().size() === 1 || !visibility) {
//            indicator.visibility = View.GONE
//        } else {
//            indicator.setViewPager(viewPager)
//        }
    }

    @SuppressLint("SetTextI18n")
    private fun updatePageNum(current: Int) {
        val count = bigImageList?.size ?: 0
        if (count < 2) {
            tvPageNum.visibility = GONE
            return
        }
        tvPageNum.visibility = VISIBLE
        tvPageNum.text = "${current + 1}/${count}"
    }
}