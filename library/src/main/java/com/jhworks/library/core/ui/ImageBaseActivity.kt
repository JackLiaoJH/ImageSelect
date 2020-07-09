package com.jhworks.library.core.ui

import android.annotation.TargetApi
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.view.MenuItem
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.jhworks.library.R

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/6/11 9:54
 */
open class ImageBaseActivity : AppCompatActivity() {
    protected var mToolbar: Toolbar? = null
    private val mStatusBarColor = Color.BLACK
    private val mToolbarColor = Color.BLACK
    private val mToolbarWidgetColor = Color.WHITE

    @DrawableRes
    private val mToolbarCancelDrawable = R.drawable.ic_sl_crop_close

    protected fun initToolBar(showTitle: Boolean) {
        if (mToolbar == null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.BLACK
            window.navigationBarColor = Color.BLACK
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

    protected open fun setupAppBar(title:String) {
        setStatusBarColor(mStatusBarColor)
        val toolbar = findViewById<Toolbar>(R.id.sl_toolbar)

        // Set all of the Toolbar coloring
        toolbar.setBackgroundColor(mToolbarColor)
        toolbar.setTitleTextColor(mToolbarWidgetColor)
        val toolbarTitle = toolbar.findViewById<TextView>(R.id.sl_toolbar_title)
        toolbarTitle.setTextColor(mToolbarWidgetColor)
        toolbarTitle.text = title

        // Color buttons inside the Toolbar
        val stateButtonDrawable = ContextCompat.getDrawable(this, mToolbarCancelDrawable)?.mutate()
        stateButtonDrawable?.setColorFilter(mToolbarWidgetColor, PorterDuff.Mode.SRC_ATOP)
        toolbar.navigationIcon = stateButtonDrawable
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setStatusBarColor(@ColorInt color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = color
            }
        }
    }

    protected open fun onBackIconClick() {}
}