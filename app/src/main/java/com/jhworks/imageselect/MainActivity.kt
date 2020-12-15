package com.jhworks.imageselect

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.jhworks.library.ImageSelector
import com.jhworks.library.core.MediaSelectConfig
import com.jhworks.library.core.vo.SelectMode
import java.util.*

class MainActivity : AppCompatActivity() {
    private var mResultText: TextView? = null
    private var mChoiceMode: RadioGroup? = null
    private var mShowCamera: RadioGroup? = null
    private lateinit var mRgTheme: RadioGroup
    private var mRequestNum: EditText? = null
    private var mImageSpanCount: EditText? = null
    private var mBtnOpenCameraOnly: Button? = null
    private var mToolbar: Toolbar? = null
    private var mSelectPath: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        mToolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(mToolbar)
        mToolbar!!.setTitle(R.string.app_name)
        mToolbar!!.setNavigationIcon(R.mipmap.ic_launcher)
        mResultText = findViewById<View>(R.id.result) as TextView
        mChoiceMode = findViewById<View>(R.id.choice_mode) as RadioGroup
        mShowCamera = findViewById<View>(R.id.show_camera) as RadioGroup
        mRgTheme = findViewById(R.id.rg_theme)
        mRequestNum = findViewById<View>(R.id.request_num) as EditText
        mImageSpanCount = findViewById<View>(R.id.image_span_count) as EditText
        mBtnOpenCameraOnly = findViewById<View>(R.id.btn_open_camera_only) as Button
        mChoiceMode!!.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.multi) {
                mRequestNum!!.isEnabled = true
            } else {
                mRequestNum!!.isEnabled = false
                mRequestNum!!.setText("")
            }
        }
        val button = findViewById<View>(R.id.button)
        button?.setOnClickListener { pickImage(false) }
        mBtnOpenCameraOnly!!.setOnClickListener { pickImage(true) }
        findViewById<View>(R.id.btn_open_video).setOnClickListener { pickVideo() }

        findViewById<View>(R.id.btn_test_crop).setOnClickListener {
            startActivity(Intent(this, TestImageCropActivity::class.java))
        }
    }

    private fun pickVideo() {
        var maxNum = 9
        if (!TextUtils.isEmpty(mRequestNum!!.text)) {
            try {
                maxNum = Integer.valueOf(mRequestNum!!.text.toString())
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
        }
        var imageSpanCount = 3
        if (!TextUtils.isEmpty(mImageSpanCount!!.text)) {
            try {
                imageSpanCount = Integer.valueOf(mImageSpanCount!!.text.toString())
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
        }
        if (mSelectPath != null && mSelectPath!!.size > 0) mSelectPath!!.clear()

        ImageSelector.startVideoAction(this@MainActivity, REQUEST_IMAGE,
                MediaSelectConfig.Builder()
                        .setSelectMode(getSelectMode())
                        .setOriginData(mSelectPath)
                        .setMaxCount(maxNum)
                        .setImageSpanCount(imageSpanCount)
                        .build())
    }

    private fun pickImage(isOpneCameraOnly: Boolean) {
        val showCamera = mShowCamera!!.checkedRadioButtonId == R.id.show
        var maxNum = 9
        if (!TextUtils.isEmpty(mRequestNum!!.text)) {
            try {
                maxNum = Integer.valueOf(mRequestNum!!.text.toString())
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
        }
        var imageSpanCount = 3
        if (!TextUtils.isEmpty(mImageSpanCount!!.text)) {
            try {
                imageSpanCount = Integer.valueOf(mImageSpanCount!!.text.toString())
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
        }
//        if (mSelectPath != null && mSelectPath!!.size > 0) mSelectPath!!.clear()

        // theme
        val theme = when (mRgTheme.checkedRadioButtonId) {
            R.id.theme_custom -> R.style.sl_theme_custom
            R.id.theme_light -> R.style.sl_theme_light
            R.id.theme_dark -> R.style.sl_theme_dark
            else -> R.style.sl_theme_light
        }

        ImageSelector.startImageAction(this@MainActivity, REQUEST_IMAGE,
                MediaSelectConfig.Builder()
                        .setSelectMode(getSelectMode())
                        .setOriginData(mSelectPath)
                        .setTheme(theme)
                        .setShowCamera(showCamera)
                        .setPlaceholderResId(R.mipmap.ic_launcher)
                        .setOpenCameraOnly(isOpneCameraOnly)
                        .setMaxCount(maxNum)
                        .setImageSpanCount(imageSpanCount)
                        .build()
        )
    }

    private fun getSelectMode(): Int {
        return if (mChoiceMode!!.checkedRadioButtonId == R.id.single)
            SelectMode.MODE_SINGLE
        else
            SelectMode.MODE_MULTI
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                mSelectPath = ImageSelector.getSelectResults(data)
                val sb = StringBuilder()
                mSelectPath?.forEach {
                    sb.append(it)
                    sb.append("\n")
                }
                mResultText?.text = sb.toString()
                Log.e("ImageSelect", "结果： $mSelectPath")
            }
        }
    }

    companion object {
        private const val REQUEST_IMAGE = 2
    }
}