package com.jhworks.library.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

/**
 * User ：LiaoJH <br/>
 * Desc ：base
 * <br/>
 * Date ：2016/7/31 0031 <br/>
 */
public class ImageBaseActivity extends AppCompatActivity {
    protected Toolbar mToolbar;
    protected Context mContext;

    protected <T> T findView(int resId) {
        return (T) findViewById(resId);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mContext = this;
    }

    protected void initToolBar(boolean showTitle) {
        if (mToolbar == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.BLACK);
        }
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(showTitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onNavagitationClick();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * deal back key
     */
    protected void onNavagitationClick() {

    }
}
