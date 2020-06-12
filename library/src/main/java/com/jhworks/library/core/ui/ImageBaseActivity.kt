package com.jhworks.library.core.ui

import android.graphics.Color
import android.os.Build
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/6/11 9:54
 */
open class ImageBaseActivity : AppCompatActivity() {
    protected var mToolbar: Toolbar? = null

    protected fun initToolBar(showTitle: Boolean) {
        if (mToolbar == null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.BLACK
        }
        setSupportActionBar(mToolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowTitleEnabled(showTitle)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackIconClick()
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    protected open fun onBackIconClick() {}
}