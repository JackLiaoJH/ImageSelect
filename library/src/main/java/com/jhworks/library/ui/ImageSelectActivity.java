package com.jhworks.library.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;

import com.jhworks.library.Constant;
import com.jhworks.library.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Author : LiaoJH
 * Desc :
 * Date: 2016/7/27
 */
public class ImageSelectActivity extends ImageBaseActivity
        implements ImageSelectorFragment.Callback {
    protected static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;

    private ArrayList<String> resultList = new ArrayList<>();
    private Button mSubmitButton;
    private int mDefaultCount = Constant.DEFAULT_IMAGE_SIZE;
    private int mMode;
    private boolean mIsShowCamera;
    private boolean mIsOnlyOpenCamera;
    private int mImageImageSpanCount = Constant.DEFAULT_IMAGE_SPAN_COUNT;

    private static Intent createIntent(Context context, ArrayList<String> originData, boolean showCamera,
                                       int maxCount, int mode, int imageSpanCount, boolean onlyOpenCamera) {
        Intent intent = new Intent(context, ImageSelectActivity.class);
        intent.putExtra(Constant.KEY_EXTRA_SHOW_CAMERA, showCamera);
        intent.putExtra(Constant.KEY_EXTRA_SELECT_COUNT, maxCount);
        if (originData != null) {
            intent.putStringArrayListExtra(Constant.KEY_EXTRA_DEFAULT_SELECTED_LIST, originData);
        }
        intent.putExtra(Constant.KEY_EXTRA_SELECT_MODE, mode);
        intent.putExtra(Constant.KEY_EXTRA_IMAGE_SPAN_COUNT, imageSpanCount);
        intent.putExtra(Constant.KEY_EXTRA_OPEN_CAMERA_ONLY, onlyOpenCamera);
        return intent;
    }

    /**
     * start image select
     *
     * @param activity
     * @param requestCode
     * @param originData
     * @param showCamera
     * @param maxCount
     * @param mode
     * @param imageSpanCount image span list count
     */
    public static void start(Activity activity, int requestCode, ArrayList<String> originData,
                             boolean showCamera, int maxCount, int mode, int imageSpanCount, boolean onlyOpenCamera) {
        if (activity == null || activity.isFinishing()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN // Permission was added in API Level 16
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE,
                    activity.getString(R.string.mis_permission_rationale),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);
        } else {
            activity.startActivityForResult(createIntent(activity, originData, showCamera, maxCount,
                    mode, imageSpanCount, onlyOpenCamera), requestCode);
        }
    }

    /**
     * start image select
     *
     * @param fragment
     * @param requestCode
     * @param originData
     * @param showCamera
     * @param maxCount
     * @param mode
     * @param imageSpanCount image span list count
     */
    public static void start(Fragment fragment, int requestCode, ArrayList<String> originData,
                             boolean showCamera, int maxCount, int mode, int imageSpanCount, boolean onlyOpenCamera) {
        if (fragment == null) return;
        final Activity activity = fragment.getActivity();
        if (activity == null || activity.isFinishing()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN // Permission was added in API Level 16
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE,
                    activity.getString(R.string.mis_permission_rationale),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);
        } else {
            fragment.startActivityForResult(createIntent(activity, originData, showCamera, maxCount,
                    mode, imageSpanCount, onlyOpenCamera), requestCode);
        }
    }

    private static void requestPermission(final Activity activity, final String permission, String rationale, final int requestCode) {
        if (activity == null || activity.isFinishing()) return;
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.mis_permission_dialog_title)
                    .setMessage(rationale)
                    .setPositiveButton(R.string.mis_permission_dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
                        }
                    })
                    .setNegativeButton(R.string.mis_permission_dialog_cancel, null)
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_READ_ACCESS_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(createIntent(this, resultList, mIsShowCamera, mDefaultCount, mMode, mImageImageSpanCount, mIsOnlyOpenCamera), requestCode);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.MIS_NO_ACTIONBAR);
        setContentView(R.layout.mis_activity_default);
        mToolbar = findView(R.id.toolbar);
        mSubmitButton = findView(R.id.commit);
        initToolBar(true);
        mToolbar.setTitle(R.string.select_phone);

        final Intent intent = getIntent();
        mDefaultCount = intent.getIntExtra(Constant.KEY_EXTRA_SELECT_COUNT, Constant.DEFAULT_IMAGE_SIZE);
        mImageImageSpanCount = intent.getIntExtra(Constant.KEY_EXTRA_IMAGE_SPAN_COUNT, Constant.DEFAULT_IMAGE_SPAN_COUNT);
        mMode = intent.getIntExtra(Constant.KEY_EXTRA_SELECT_MODE, Constant.MODE_MULTI);
        mIsShowCamera = intent.getBooleanExtra(Constant.KEY_EXTRA_SHOW_CAMERA, true);
        mIsOnlyOpenCamera = intent.getBooleanExtra(Constant.KEY_EXTRA_OPEN_CAMERA_ONLY, false);
        if (mMode == Constant.MODE_MULTI && intent.hasExtra(Constant.KEY_EXTRA_DEFAULT_SELECTED_LIST)) {
            resultList = intent.getStringArrayListExtra(Constant.KEY_EXTRA_DEFAULT_SELECTED_LIST);
        }

        if (mMode == Constant.MODE_MULTI) {
            updateDoneText(resultList);
            mSubmitButton.setVisibility(View.VISIBLE);
            mSubmitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (resultList != null && resultList.size() > 0) {
                        // Notify success
                        Intent data = new Intent();
                        data.putStringArrayListExtra(Constant.KEY_EXTRA_RESULT, resultList);
                        setResult(RESULT_OK, data);
                    } else {
                        setResult(RESULT_CANCELED);
                    }
                    finish();
                }
            });
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }

        if (savedInstanceState == null) {
            Bundle bundle = new Bundle();
            bundle.putInt(Constant.KEY_EXTRA_SELECT_COUNT, mDefaultCount);
            bundle.putInt(Constant.KEY_EXTRA_SELECT_MODE, mMode);
            bundle.putBoolean(Constant.KEY_EXTRA_SHOW_CAMERA, mIsShowCamera);
            bundle.putBoolean(Constant.KEY_EXTRA_OPEN_CAMERA_ONLY, mIsOnlyOpenCamera);
            bundle.putStringArrayList(Constant.KEY_EXTRA_DEFAULT_SELECTED_LIST, resultList);
            bundle.putInt(Constant.KEY_EXTRA_IMAGE_SPAN_COUNT, mImageImageSpanCount);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.image_grid, Fragment.instantiate(this, ImageSelectorFragment.class.getName(), bundle))
                    .commit();
        }

    }

    @Override
    protected void onNavagitationClick() {
        super.onNavagitationClick();
        setResult(RESULT_CANCELED);
    }

    /**
     * Update done button by select image data
     *
     * @param resultList selected image data
     */
    private void updateDoneText(ArrayList<String> resultList) {
        int size = 0;
        if (resultList == null || resultList.size() <= 0) {
            mSubmitButton.setText(R.string.mis_action_done);
            mSubmitButton.setEnabled(false);
        } else {
            size = resultList.size();
            mSubmitButton.setEnabled(true);
        }
        mSubmitButton.setText(getString(R.string.mis_action_button_string,
                getString(R.string.mis_action_done), size, mDefaultCount));
    }

    @Override
    public void onSingleImageSelected(String path) {
        Intent data = new Intent();
        resultList.add(path);
        data.putStringArrayListExtra(Constant.KEY_EXTRA_RESULT, resultList);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onImageSelected(String path) {
        if (!resultList.contains(path)) {
            resultList.add(path);
        }
        updateDoneText(resultList);
    }

    @Override
    public void onImageUnselected(String path) {
        if (resultList.contains(path)) {
            resultList.remove(path);
        }
        updateDoneText(resultList);
    }

    @Override
    public void onCameraShot(File imageFile) {
        if (imageFile != null) {
            // notify system the image has change
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));

            Intent data = new Intent();
            resultList.add(imageFile.getAbsolutePath());
            data.putStringArrayListExtra(Constant.KEY_EXTRA_RESULT, resultList);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public void onImageSelectList(ArrayList<String> imageList) {
        if (resultList.size() > 0)
            resultList.clear();
        resultList.addAll(imageList);
        updateDoneText(resultList);
    }
}
