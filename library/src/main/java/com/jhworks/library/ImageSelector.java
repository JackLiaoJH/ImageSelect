package com.jhworks.library;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.jhworks.library.ui.ImageSelectActivity;

import java.util.ArrayList;

/**
 * 图片选择器
 * Created by nereo on 16/3/17.
 */
public class ImageSelector {
    public static final String EXTRA_RESULT = Constant.KEY_EXTRA_RESULT;
    private boolean mShowCamera = true;
    private int mMaxCount = Constant.DEFAULT_IMAGE_SIZE;
    private int mMode = Constant.MODE_MULTI;
    private ArrayList<String> mOriginData;
    private static ImageSelector sSelector;
    private int mImageSpanCount = Constant.DEFAULT_IMAGE_SPAN_COUNT;
    private boolean mOpenCameraOnly = false;  // is open camera only

    private ImageSelector() {
    }

    public static ImageSelector create() {
        if (sSelector == null) {
            sSelector = new ImageSelector();
        }
        return sSelector;
    }

    /**
     * show camera or no in image list
     *
     * @param show true :show
     * @return
     */
    public ImageSelector showCamera(boolean show) {
        mShowCamera = show;
        return sSelector;
    }

    /**
     * set select multi count
     *
     * @param count select multi count
     * @return
     */
    public ImageSelector count(int count) {
        mMaxCount = count;
        return sSelector;
    }

    /**
     * set select single mode
     *
     * @return
     */
    public ImageSelector single() {
        mMode = Constant.MODE_SINGLE;
        return sSelector;
    }

    /**
     * set select multi mode
     *
     * @return
     */
    public ImageSelector multi() {
        mMode = Constant.MODE_MULTI;
        return sSelector;
    }

    /**
     * set origin image list resource
     *
     * @param images origin image list resource
     * @return
     */
    public ImageSelector origin(ArrayList<String> images) {
        mOriginData = images;
        return sSelector;
    }

    /**
     * image span count
     *
     * @param spanCount span count ,default:4
     * @return
     */
    public ImageSelector spanCount(int spanCount) {
        mImageSpanCount = spanCount;
        return sSelector;
    }

    public ImageSelector openCameraOnly(boolean openCameraOnly) {
        mOpenCameraOnly = openCameraOnly;
        return sSelector;
    }

    public void start(Activity activity, int requestCode) {
        ImageSelectActivity.start(activity, requestCode, mOriginData, mShowCamera, mMaxCount,
                mMode, mImageSpanCount,mOpenCameraOnly);
    }

    public void start(Fragment fragment, int requestCode) {
        ImageSelectActivity.start(fragment, requestCode, mOriginData, mShowCamera, mMaxCount,
                mMode, mImageSpanCount,mOpenCameraOnly);
    }

    private boolean hasPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // Permission was added in API Level 16
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
}
