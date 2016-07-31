package com.jhworks.library.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.jhworks.library.Constant;
import com.jhworks.library.R;
import com.jhworks.library.bean.Media;
import com.jhworks.library.view.HackyViewPager;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;

/**
 * User ：LiaoJH <br/>
 * Desc ： 图片查看界面
 * <br/>
 * Date ：2016/7/30 0030 <br/>
 */
public class ImageActivity extends ImageBaseActivity implements ViewPager.OnPageChangeListener {

    private AppCompatCheckBox mCheckBox;
    private HackyViewPager mViewPager;
    private int mCurrentPosition;
    ArrayList<Media> mAllMediaList;
    ArrayList<String> mSelectList = new ArrayList<>();
    int maxImageCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        mToolbar = findView(R.id.toolbar);
        mCheckBox = findView(R.id.check_image);
        mViewPager = findView(R.id.view_pager);
        initToolBar(false);

        Intent intent = getIntent();
        if (intent != null) {
            mAllMediaList = intent.getParcelableArrayListExtra(Constant.KEY_EXTRA_IMAGE_LIST);
            mCurrentPosition = intent.getIntExtra(Constant.KEY_EXTRA_CURRENT_POSITION, 0);
            maxImageCount = intent.getIntExtra(Constant.KEY_EXTRA_SELECT_COUNT, Constant.DEFAULT_IMAGE_SIZE);
        }
        if (mAllMediaList == null || mAllMediaList.size() == 0) {
            finish();
            return;
        }

        for (Media media : mAllMediaList) {
            if (media != null && media.isSelect) {
                mSelectList.add(media.path);
            }
        }

        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (mAllMediaList != null && mAllMediaList.size() > 0) {
                    if (maxImageCount == mSelectList.size() && b) {
                        Toast.makeText(ImageActivity.this, R.string.mis_msg_amount_limit, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Media media = mAllMediaList.get(mCurrentPosition);
                    if (media != null) {
                        media.isSelect = b;
                        if (b)
                            mSelectList.add(media.path);
                        else
                            mSelectList.remove(media.path);
                        mCheckBox.setButtonDrawable(media.isSelect ? R.drawable.ic_check_circle_green_24dp
                                : R.drawable.ic_check_circle_while_24dp);
                    }
                }
            }
        });
        ImagePageAdapter adapter = new ImagePageAdapter(this, mAllMediaList, Glide.with(this));
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(mCurrentPosition);
        mViewPager.addOnPageChangeListener(this);
        setCheckBoxSelect(mCurrentPosition, true);
        if (mAllMediaList != null && mAllMediaList.size() > 0) {
            Media media = mAllMediaList.get(mCurrentPosition);
            if (media != null && media.isSelect) {
                mSelectList.add(media.path);
            }
        }
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mCurrentPosition = position;
        setCheckBoxSelect(position, false);
    }

    private void setCheckBoxSelect(int position, boolean setCheckStatus) {
        if (mAllMediaList != null && mAllMediaList.size() > 0) {
            Media media = mAllMediaList.get(position);
            if (media != null) {
                if (setCheckStatus)
                    mCheckBox.setChecked(media.isSelect);
                mCheckBox.setButtonDrawable(media.isSelect ? R.drawable.ic_check_circle_green_24dp
                        : R.drawable.ic_check_circle_while_24dp);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onBackPressed() {
        setIntentResult();
        super.onBackPressed();
    }

    @Override
    protected void onNavagitationClick() {
        super.onNavagitationClick();
        setIntentResult();
    }

    private void setIntentResult() {
        ArrayList<String> selectList = new ArrayList<>();
        for (Media media : mAllMediaList) {
            if (media.isSelect) {
                selectList.add(media.path);
            }
        }
        Intent intent = new Intent();
        intent.putStringArrayListExtra(Constant.KEY_EXTRA_IMAGE_SELECT_LIST, selectList);
        setResult(RESULT_OK, intent);
    }

    private static class ImagePageAdapter extends PagerAdapter {
        private Context mContext;
        private ArrayList<Media> mMedias;
        private RequestManager mRequestManager;

        public ImagePageAdapter(Context context, ArrayList<Media> medias, RequestManager requestManager) {
            mContext = context;
            mMedias = medias;
            mRequestManager = requestManager;
        }

        @Override
        public int getCount() {
            return mMedias == null ? 0 : mMedias.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(mContext);
            Media media = mMedias.get(position);
            if (media != null) {
                mRequestManager
                        .load(media.path)
                        .placeholder(R.drawable.ic_photo_gray_63dp)
                        .fitCenter()
                        .into(photoView)
                ;
            }
            container.addView(photoView);
            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
