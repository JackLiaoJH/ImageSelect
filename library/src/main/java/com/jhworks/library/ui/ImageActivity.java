package com.jhworks.library.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.jhworks.library.Constant;
import com.jhworks.library.R;
import com.jhworks.library.bean.DataBundle;
import com.jhworks.library.bean.Media;
import com.jhworks.library.view.HackyViewPager;

import java.util.ArrayList;


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
            mCurrentPosition = intent.getIntExtra(Constant.KEY_EXTRA_CURRENT_POSITION, 0);
            maxImageCount = intent.getIntExtra(Constant.KEY_EXTRA_SELECT_COUNT, Constant.DEFAULT_IMAGE_SIZE);
        }
        mAllMediaList = DataBundle.get().getMediaList();
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
                        mCheckBox.setButtonDrawable(media.isSelect ? R.mipmap.ic_select_pressed
                                : R.mipmap.ic_select_normal);
                    }
                }
            }
        });
        ImagePageAdapter adapter = new ImagePageAdapter(this, mAllMediaList);
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
        if (mAllMediaList != null && mAllMediaList.size() > 0
                && position >= 0 && position < mAllMediaList.size()) {
            Media media = mAllMediaList.get(position);
            if (media != null) {
                if (setCheckStatus)
                    mCheckBox.setChecked(media.isSelect);
                mCheckBox.setButtonDrawable(media.isSelect ? R.mipmap.ic_select_pressed
                        : R.mipmap.ic_select_normal);
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
        private RequestOptions mRequestOptions;

        ImagePageAdapter(Context context, ArrayList<Media> medias) {
            mContext = context;
            mMedias = medias;
            mRequestOptions = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.mipmap.ic_image_default)
                    .error(R.mipmap.ic_image_default)
                    .fitCenter()
                    .priority(Priority.HIGH);
        }

        @Override
        public int getCount() {
            return mMedias == null ? 0 : mMedias.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(mContext);
            Media media = mMedias.get(position);
            if (media != null) {
                Glide.with(container.getContext())
                        .load(media.path)
                        .apply(mRequestOptions)
                        .into(photoView)
                ;
            }
            container.addView(photoView);
            return photoView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }
}
