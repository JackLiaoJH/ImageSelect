package com.jhworks.library.core.ui

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.github.chrisbanes.photoview.PhotoView
import com.jhworks.library.ImageSelector
import com.jhworks.library.R
import com.jhworks.library.core.MediaConstant
import com.jhworks.library.core.vo.MediaUiConfigVo
import com.jhworks.library.core.vo.MediaVo
import com.jhworks.library.view.HackyViewPager

/**
 * 图片查看界面
 */
class ImageDetailActivity : ImageBaseActivity(), OnPageChangeListener {
    private lateinit var mCheckBox: AppCompatCheckBox
    private lateinit var mTvToolbarOk: TextView
    private lateinit var mTvImageSelectCount: TextView
    private lateinit var mViewPager: HackyViewPager

    private var mCurrentPosition = 0
    private var mAllMediaList: MutableList<MediaVo>? = null
    private var maxImageCount = 0

    override fun setLayout(): Int = R.layout.activity_sl_image_detail

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarColor()

        mCheckBox = findViewById(R.id.sl_check_image)
        mTvToolbarOk = findViewById(R.id.sl_tv_detail_ok)
        mTvImageSelectCount = findViewById(R.id.sl_tv_select_image_count)
        mViewPager = findViewById(R.id.sl_view_pager)

        mCurrentPosition = intent.getIntExtra(MediaConstant.KEY_EXTRA_CURRENT_POSITION, 0)
        maxImageCount = intent.getIntExtra(MediaConstant.KEY_EXTRA_SELECT_COUNT, MediaConstant.DEFAULT_IMAGE_SIZE)
        mAllMediaList = MediaConstant.getAllMediaList()

        if (mAllMediaList == null || mAllMediaList!!.isEmpty()) {
            finish()
            return
        }

        updateImageCount()
        mCheckBox.setOnClickListener {
            val b = mCheckBox.isChecked
            val selectList = MediaConstant.getSelectMediaList()
            if (maxImageCount <= selectList.size && b) {
                mCheckBox.isChecked = false
                Toast.makeText(this@ImageDetailActivity, R.string.sl_msg_amount_limit, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mAllMediaList?.let {
                val media = it[mCurrentPosition]
                media.isSelect = b
                if (b) {
                    if (!selectList.contains(media)) selectList.add(media)
                } else {
                    if (selectList.contains(media)) selectList.remove(media)
                }
            }

            updateImageCount()
        }
        mTvToolbarOk.setOnClickListener { onBackPressed() }

        val adapter = ImagePageAdapter(this, mAllMediaList!!)
        mViewPager.adapter = adapter
        mViewPager.currentItem = mCurrentPosition
        mViewPager.addOnPageChangeListener(this)
        setCheckBoxSelect(mCurrentPosition)
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    override fun onPageSelected(position: Int) {
        mCurrentPosition = position
        setCheckBoxSelect(position)
    }

    private fun setCheckBoxSelect(position: Int) {
        if (mAllMediaList != null
                && mAllMediaList!!.isNotEmpty()
                && position >= 0
                && position < mAllMediaList!!.size) {
            mCheckBox.isChecked = mAllMediaList!![position].isSelect
        }
    }

    private fun updateImageCount() {
        val selectList = MediaConstant.getSelectMediaList()
        mTvImageSelectCount.text = getString(R.string.sl_select_image_count, selectList.size, maxImageCount)
    }

    override fun onPageScrollStateChanged(state: Int) {}
    override fun onBackPressed() {
        setIntentResult()
        super.onBackPressed()
    }

    override fun onBackIconClick() {
        onBackPressed()
    }

    private fun setIntentResult() {
        setResult(Activity.RESULT_OK)
    }

    private class ImagePageAdapter internal constructor(
            private val mContext: Context,
            private val mMedias: MutableList<MediaVo>)
        : PagerAdapter() {
        override fun getCount(): Int {
            return mMedias.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val photoView = PhotoView(mContext)
            val media = mMedias[position]
            val uiConfig = MediaUiConfigVo(media.path)
            ImageSelector.getImageEngine().loadDetailImage(photoView, uiConfig)
            container.addView(photoView)
            return photoView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }
    }
}