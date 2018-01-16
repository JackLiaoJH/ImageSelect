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
import com.jhworks.library.bean.MediaSelectConfig;

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
    private MediaSelectConfig mMediaSelectConfig;

    private static Intent createIntent(Context context, MediaSelectConfig mediaSelectConfig) {
        Intent intent = new Intent(context, ImageSelectActivity.class);
        intent.putExtra(Constant.KEY_MEDIA_SELECT_CONFIG, mediaSelectConfig);
        return intent;
    }

    /**
     * start image select
     *
     * @param activity          -
     * @param requestCode       -
     * @param mediaSelectConfig -
     */
    public static void start(Activity activity, int requestCode, MediaSelectConfig mediaSelectConfig) {
        if (activity == null || activity.isFinishing()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN // Permission was added in API Level 16
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE,
                    activity.getString(R.string.mis_permission_rationale),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);
        } else {
            activity.startActivityForResult(createIntent(activity, mediaSelectConfig), requestCode);
        }
    }

    /**
     * start image select
     *
     * @param fragment          -
     * @param requestCode       -
     * @param mediaSelectConfig -
     */
    public static void start(Fragment fragment, int requestCode, MediaSelectConfig mediaSelectConfig) {
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
            fragment.startActivityForResult(createIntent(activity, mediaSelectConfig), requestCode);
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
                startActivityForResult(createIntent(this, mMediaSelectConfig), requestCode);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.MIS_NO_ACTIONBAR);
        setContentView(R.layout.activity_image_select);
        mToolbar = findView(R.id.toolbar);
        mSubmitButton = findView(R.id.commit);
        initToolBar(true);
        mToolbar.setTitle(R.string.select_phone);

        final Intent intent = getIntent();
        mMediaSelectConfig = intent.getParcelableExtra(Constant.KEY_MEDIA_SELECT_CONFIG);
        if (mMediaSelectConfig.getSelectMode() == MediaSelectConfig.MODE_MULTI
                && mMediaSelectConfig.getOriginData() != null) {
            resultList = mMediaSelectConfig.getOriginData();
        }

        if (mMediaSelectConfig.getSelectMode() == MediaSelectConfig.MODE_MULTI) {
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
            bundle.putParcelable(Constant.KEY_MEDIA_SELECT_CONFIG, mMediaSelectConfig);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.image_grid, Fragment.instantiate(this,
                            ImageSelectorFragment.class.getName(), bundle))
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
                getString(R.string.mis_action_done), size, mMediaSelectConfig.getMaxCount()));
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
