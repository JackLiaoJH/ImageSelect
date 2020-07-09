package com.jhworks.library.crop

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.jhworks.library.R
import com.jhworks.library.core.ui.ImageBaseActivity

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/7/7 14:19
 */
class ImageCropActivity : ImageBaseActivity() {

    @DrawableRes
    private val mToolbarCropDrawable = R.drawable.ic_sl_crop_done
    private var mToolbarWidgetColor = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sl_image_crop)

        val path = intent.getStringExtra("path")

        mToolbarWidgetColor = Color.WHITE

        setupAppBar(getString(R.string.sl_menu_crop))
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.sl_crop_menu, menu)

        val menuItemCrop = menu.findItem(R.id.menu_crop)
        val menuItemCropIcon = ContextCompat.getDrawable(this, mToolbarCropDrawable)
        if (menuItemCropIcon != null) {
            menuItemCropIcon.mutate()
            menuItemCropIcon.setColorFilter(mToolbarWidgetColor, PorterDuff.Mode.SRC_ATOP)
            menuItemCrop.icon = menuItemCropIcon
        }

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.menu_crop).isVisible = true
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_crop -> {
                cropAndSaveImage()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun cropAndSaveImage() {

    }
}