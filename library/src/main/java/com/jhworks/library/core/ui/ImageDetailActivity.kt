package com.jhworks.library.core.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.github.chrisbanes.photoview.PhotoView
import com.jhworks.library.ImageSelector
import com.jhworks.library.R
import com.jhworks.library.core.MediaConstant
import com.jhworks.library.core.MediaConstant.KEY_EXTRA_ALL_IMAGE_LIST
import com.jhworks.library.core.vo.MediaUiConfigVo
import com.jhworks.library.core.vo.MediaVo
import com.jhworks.library.view.HackyViewPager
import java.util.*

/**
 * 图片查看界面
 */
class ImageDetailActivity : ImageBaseActivity(), OnPageChangeListener {
    private lateinit var mCheckBox: AppCompatCheckBox
    private lateinit var mViewPager: HackyViewPager

    private var mCurrentPosition = 0
    private var mAllMediaList: ArrayList<MediaVo>? = null
    private var mSelectList = arrayListOf<String>()
    private var maxImageCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_detail)
        mToolbar = findViewById(R.id.toolbar)
        mCheckBox = findViewById(R.id.check_image)
        mViewPager = findViewById(R.id.view_pager)
        initToolBar(false)

        mCurrentPosition = intent.getIntExtra(MediaConstant.KEY_EXTRA_CURRENT_POSITION, 0)
        maxImageCount = intent.getIntExtra(MediaConstant.KEY_EXTRA_SELECT_COUNT, MediaConstant.DEFAULT_IMAGE_SIZE)
        mAllMediaList = intent.getParcelableArrayListExtra(KEY_EXTRA_ALL_IMAGE_LIST)

        if (mAllMediaList == null || mAllMediaList!!.isEmpty()) {
            finish()
            return
        }
        for (media in mAllMediaList!!) {
            if (media.isSelect && media.path != null) {
                mSelectList.add(media.path)
            }
        }
        mCheckBox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, b ->
            if (mAllMediaList != null && mAllMediaList!!.isNotEmpty()) {
                if (maxImageCount == mSelectList.size && b) {
                    Toast.makeText(this@ImageDetailActivity, R.string.mis_msg_amount_limit, Toast.LENGTH_SHORT).show()
                    return@OnCheckedChangeListener
                }
                val media = mAllMediaList!![mCurrentPosition]
                if (media.path != null) {
                    media.isSelect = b
                    if (b) mSelectList.add(media.path) else mSelectList.remove(media.path)
                    mCheckBox.setButtonDrawable(
                            if (media.isSelect) R.drawable.ic_select_pressed else R.drawable.ic_select_normal)
                }
            }
        })
        val adapter = ImagePageAdapter(this, mAllMediaList!!)
        mViewPager.adapter = adapter
        mViewPager.currentItem = mCurrentPosition
        mViewPager.addOnPageChangeListener(this)
        setCheckBoxSelect(mCurrentPosition, true)

        val media = mAllMediaList!![mCurrentPosition]
        if (media.path != null && media.isSelect) {
            mSelectList.add(media.path)
        }

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    override fun onPageSelected(position: Int) {
        mCurrentPosition = position
        setCheckBoxSelect(position, false)
    }

    private fun setCheckBoxSelect(position: Int, setCheckStatus: Boolean) {
        if (mAllMediaList != null
                && mAllMediaList!!.isNotEmpty()
                && position >= 0
                && position < mAllMediaList!!.size) {
            val media = mAllMediaList!![position]
            if (setCheckStatus) mCheckBox.isChecked = media.isSelect
            mCheckBox.setButtonDrawable(
                    if (media.isSelect) R.drawable.ic_select_pressed else R.drawable.ic_select_normal)
        }
    }

    override fun onPageScrollStateChanged(state: Int) {}
    override fun onBackPressed() {
        setIntentResult()
        super.onBackPressed()
    }

    override fun onBackIconClick() {
        super.onBackIconClick()
        setIntentResult()
    }

    private fun setIntentResult() {
        val selectList = arrayListOf<String>()
        for (media in mAllMediaList!!) {
            if (media.isSelect && media.path != null) {
                selectList.add(media.path)
            }
        }
        val intent = Intent()
        intent.putStringArrayListExtra(MediaConstant.KEY_EXTRA_IMAGE_SELECT_LIST, selectList)
        setResult(Activity.RESULT_OK, intent)
    }

    private class ImagePageAdapter internal constructor(
            private val mContext: Context,
            private val mMedias: MutableList<MediaVo>)
        : PagerAdapter() {
        //        private val mRequestOptions: RequestOptions
        override fun getCount(): Int {
            return mMedias.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val photoView = PhotoView(mContext)
            val media = mMedias[position]
            // todo
//            Glide.with(container.context)
//                    .load(media.path)
//                    .apply(mRequestOptions)
//                    .into(photoView)
            val uiConfig = MediaUiConfigVo(media.path)
            ImageSelector.getImageEngine().loadImage(photoView, uiConfig)
            container.addView(photoView)
            return photoView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        init {
//            mRequestOptions = RequestOptions()
//                    .centerCrop()
//                    .placeholder(R.mipmap.ic_image_default)
//                    .error(R.mipmap.ic_image_default)
//                    .fitCenter()
//                    .priority(Priority.HIGH)
        }
    }
}