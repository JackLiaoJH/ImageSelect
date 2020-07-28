package com.jhworks.imageselect.crop

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.jhworks.imageselect.R
import com.jhworks.library.core.ui.ImageBaseActivity

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/7/15 14:26
 */
class ResultActivity : ImageBaseActivity() {

    private lateinit var ivResult: AppCompatImageView

    companion object {
        fun start(context: Context, path: Uri) {
            context.startActivity(Intent(context, ResultActivity::class.java).apply {
                putExtra("path", path)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sl_result)

        ivResult = findViewById(R.id.iv_sl_result)

        val path = intent.getParcelableExtra<Uri>("path")

        Glide.with(this)
                .load(path)
                .into(ivResult)
    }
}