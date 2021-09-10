package com.jhworks.library.core.ui

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.jhworks.library.R
import com.jhworks.library.core.MediaConstant
import com.jhworks.library.core.vo.MediaConfigVo
import com.jhworks.library.utils.SlScreenUtils

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/6/11 9:54
 */
abstract class ImageBaseActivity : AppCompatActivity() {
    protected var mToolbar: Toolbar? = null
    protected var mMediaConfig: MediaConfigVo? = null

    protected fun initToolBarConfig(showTitle: Boolean = true, isSHowNavIcon: Boolean = true) {
        if (mToolbar == null) return

        supportActionBar?.setDisplayHomeAsUpEnabled(isSHowNavIcon)
        supportActionBar?.setDisplayShowTitleEnabled(showTitle)

        if (!isSHowNavIcon) return
        updateNavIcon(mToolbar?.navigationIcon)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mMediaConfig = intent.getParcelableExtra(MediaConstant.KEY_MEDIA_SELECT_CONFIG)
        val theme = mMediaConfig?.theme ?: R.style.sl_theme_light
        setTheme(theme)
        super.onCreate(savedInstanceState)
        setContentView(setLayout())

        mToolbar = findViewById(R.id.sl_toolbar)
        if (mToolbar == null) return

        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
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

    protected fun updateNavIcon(navDrawable: Drawable?): Drawable? {
        navDrawable ?: return null

        val ta = theme.obtainStyledAttributes(intArrayOf(R.attr.toolbar_title_color))
        val color = ta.getColor(0, 0)
        ta.recycle()
        navDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        return navDrawable
    }

    protected fun setStatusBarColor(
        @ColorRes color: Int = R.color.sl_dark_primary,
        isOpenImmersion: Boolean = false
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isOpenImmersion) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            }
            window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window?.statusBarColor = SlScreenUtils.getColor(this, color)
            window.navigationBarColor = SlScreenUtils.getColor(this, R.color.sl_dark_primary)
        }
    }

    protected open fun onBackIconClick() {}

    protected abstract fun setLayout(): Int
}