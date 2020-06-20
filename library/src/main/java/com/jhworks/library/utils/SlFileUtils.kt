package com.jhworks.library.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

/**
 * 文件操作类
 */
object SlFileUtils {
    private const val JPEG_FILE_PREFIX = "IMG_"
    private const val JPEG_FILE_SUFFIX = ".jpg"

    private const val EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE"

    @Throws(IOException::class)
    fun createTmpFile(context: Context): File {
        var dir: File?
        if (TextUtils.equals(Environment.getExternalStorageState(), Environment.MEDIA_MOUNTED)) {
            dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            if (!dir.exists()) {
                dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Camera")
                if (!dir.exists()) {
                    dir = getCacheDirectory(context, true)
                }
            }
        } else {
            dir = getCacheDirectory(context, true)
        }
        return File.createTempFile(JPEG_FILE_PREFIX, JPEG_FILE_SUFFIX, dir)
    }

    fun getUriFromPath(context: Context, path: String?): Uri? {
        if (path == null) return null
        try {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                        context,
                        "${context.applicationInfo?.packageName}.sl.fileprovider",
                        File(path)
                )
            } else {
                Uri.fromFile(File(path))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


    /**
     * Returns application cache directory. Cache directory will be created on SD card
     * *("/Android/data/[app_package_name]/cache")* if card is mounted and app has appropriate permission. Else -
     * Android defines cache directory on device's file system.
     *
     * @param context Application context
     * @return Cache [directory][File].
     * **NOTE:** Can be null in some unpredictable cases (if SD card is unmounted and
     * [Context.getCacheDir()][android.content.Context.getCacheDir] returns null).
     */
    fun getCacheDirectory(context: Context): File {
        return getCacheDirectory(context, true)
    }

    /**
     * Returns application cache directory. Cache directory will be created on SD card
     * *("/Android/data/[app_package_name]/cache")* (if card is mounted and app has appropriate permission) or
     * on device's file system depending incoming parameters.
     *
     * @param context        Application context
     * @param preferExternal Whether prefer external location for cache
     * @return Cache [directory][File].
     * **NOTE:** Can be null in some unpredictable cases (if SD card is unmounted and
     * [Context.getCacheDir()][android.content.Context.getCacheDir] returns null).
     */
    @SuppressLint("SdCardPath")
    fun getCacheDirectory(context: Context, preferExternal: Boolean): File {
        var appCacheDir: File? = null
        val externalStorageState = try {
            Environment.getExternalStorageState()
        } catch (e: NullPointerException) { // (sh)it happens (Issue #660)
            ""
        } catch (e: IncompatibleClassChangeError) { // (sh)it happens too (Issue #989)
            ""
        }
        if (preferExternal
                && Environment.MEDIA_MOUNTED == externalStorageState
                && hasExternalStoragePermission(context)) {
            appCacheDir = getExternalCacheDir(context)
        }
        if (appCacheDir == null) {
            appCacheDir = context.cacheDir
        }
        if (appCacheDir == null) {
            val cacheDirPath = "/data/data/${context.packageName}/cache/"
            appCacheDir = File(cacheDirPath)
        }
        return appCacheDir
    }

    /**
     * Returns individual application cache directory (for only image caching from ImageLoader). Cache directory will be
     * created on SD card *("/Android/data/[app_package_name]/cache/uil-images")* if card is mounted and app has
     * appropriate permission. Else - Android defines cache directory on device's file system.
     *
     * @param context Application context
     * @param cacheDir Cache directory path (e.g.: "AppCacheDir", "AppDir/cache/images")
     * @return Cache [directory][File]
     */
    fun getIndividualCacheDirectory(context: Context, cacheDir: String): File {
        val appCacheDir = getCacheDirectory(context)
        var individualCacheDir = File(appCacheDir, cacheDir)
        if (!individualCacheDir.exists()) {
            if (!individualCacheDir.mkdir()) {
                individualCacheDir = appCacheDir
            }
        }
        return individualCacheDir
    }

    private fun getExternalCacheDir(context: Context): File? {
        val dataDir = File(File(Environment.getExternalStorageDirectory(), "Android"), "data")
        val appCacheDir = File(File(dataDir, context.packageName), "cache")
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                return null
            }
            try {
                File(appCacheDir, ".nomedia").createNewFile()
            } catch (e: IOException) {
            }
        }
        return appCacheDir
    }

    private fun hasExternalStoragePermission(context: Context): Boolean {
        val perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION)
        return perm == PackageManager.PERMISSION_GRANTED
    }
}