package com.jhworks.imageselect.utils

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.signature.EmptySignature
import com.bumptech.glide.util.Util
import java.lang.Exception
import java.security.MessageDigest

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2021/9/10 09:20
 */
object GlideUtils {
    /**
     * Glide缓存存储路径：/data/data/your_packagexxxxxxx/cache/image_manager_disk_cache
     * Glide文件名生成规则函数 : 4.0+ 版本
     *
     * @param url 图片地址url
     * @return 返回图片在磁盘缓存的key值
     */
    fun getGlide4_SafeKey(url: String?): String? {
        try {
            val messageDigest = MessageDigest.getInstance("SHA-256")
            val signature = EmptySignature.obtain()
            signature.updateDiskCacheKey(messageDigest)
            GlideUrl(url).updateDiskCacheKey(messageDigest)
            val safeKey = Util.sha256BytesToHex(messageDigest.digest())
            return "$safeKey.0"
        } catch (e: Exception) {
        }
        return null
    }

    fun getGlideLocalCachePath(context: Context?, url: String?): String? {
        context ?: return null
        val key = getGlide4_SafeKey(url)
        if (!TextUtils.isEmpty(key)) {
            val path = "${context.cacheDir.path}/image_manager_disk_cache/%s"
            return String.format(path, key)
        }
        return null
    }
}