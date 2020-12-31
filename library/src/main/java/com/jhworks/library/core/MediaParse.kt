package com.jhworks.library.core

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import com.jhworks.library.R
import com.jhworks.library.core.vo.FolderVo
import com.jhworks.library.core.vo.MediaType
import com.jhworks.library.core.vo.MediaVo
import java.io.File

/**
 * Parse media data
 * @author jackson
 * @version 1.0
 * @date 2020/6/11 9:36
 */
object MediaParse {
    private const val KEY_TYPE_CATEGORY = "path"

    private val IMAGE_PROJECTION = arrayOf(
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DATE_TAKEN,
            MediaStore.Images.ImageColumns.DATE_MODIFIED,
            MediaStore.Images.ImageColumns.MIME_TYPE,
            MediaStore.Images.ImageColumns.ORIENTATION,
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.DISPLAY_NAME,
            MediaStore.Images.ImageColumns.DATE_ADDED,
            MediaStore.Images.ImageColumns.SIZE)

    private val VIDEO_PROJECTION = arrayOf(
            MediaStore.Video.VideoColumns._ID,
            MediaStore.Video.VideoColumns.DATE_TAKEN,
            MediaStore.Video.VideoColumns.DATE_MODIFIED,
            MediaStore.Video.VideoColumns.MIME_TYPE,
            MediaStore.Images.ImageColumns.ORIENTATION,
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.DISPLAY_NAME,
            MediaStore.Images.ImageColumns.DATE_ADDED,
            MediaStore.Images.ImageColumns.SIZE,
            MediaStore.Video.VideoColumns.DURATION)
    val MEDIA_IMAGE_URI: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val MEDIA_VIDEO_URI: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI


    fun queryImages(context: Context, mediaId: Int, bundle: Bundle?,
                    resultFolder: MutableList<FolderVo>): MutableList<MediaVo>? {
        val selectAction = StringBuilder()
        var selectArgs: Array<String>? = null
        if (mediaId == R.id.loader_all_media_store_data) {
            selectAction
                    .append(IMAGE_PROJECTION[8]).append(">0 AND ")
                    .append(IMAGE_PROJECTION[3]).append("=? OR ")
                    .append(IMAGE_PROJECTION[3]).append("=? ")
            selectArgs = arrayOf("image/jpeg", "image/png")
        } else if (mediaId == R.id.loader_category_media_store_data) {
            selectAction.append(IMAGE_PROJECTION[8]).append(">0 ")
            if (bundle != null) {
                selectAction
                        .append(" AND ")
                        .append(IMAGE_PROJECTION[5]).append(" like '%")
                        .append(bundle.getString(KEY_TYPE_CATEGORY)).append("%'")
            }
        }
        val cursor = context.contentResolver.query(
                MEDIA_IMAGE_URI, IMAGE_PROJECTION, selectAction.toString(),
                selectArgs, "${IMAGE_PROJECTION[7]} DESC"
        )

        return query(MEDIA_IMAGE_URI, cursor, MediaType.IMAGE, resultFolder)
    }

    fun queryVideos(context: Context, resultFolder: MutableList<FolderVo>): MutableList<MediaVo>? {
        try {
            val cursor = context.contentResolver.query(
                    MEDIA_VIDEO_URI, VIDEO_PROJECTION, null, null, "${VIDEO_PROJECTION[1]} DESC"
            )
            return query(MEDIA_VIDEO_URI, cursor, MediaType.VIDEO, resultFolder)
        } catch (e: IllegalArgumentException) {
            // nothing
        }
        return null
    }

    private fun query(contentUri: Uri, cursor: Cursor?, @MediaType type: Int,
                      resultFolder: MutableList<FolderVo>): MutableList<MediaVo>? {
        val data = arrayListOf<MediaVo>()
        if (cursor == null) return data
        cursor.use {
            while (it.moveToNext()) {

                val id = it.getLong(it.getColumnIndexOrThrow(IMAGE_PROJECTION[0]))
                val dateTaken = it.getLong(it.getColumnIndexOrThrow(IMAGE_PROJECTION[1]))
                val mimeType = it.getString(it.getColumnIndexOrThrow(IMAGE_PROJECTION[3]))
                val dateModified = it.getLong(it.getColumnIndexOrThrow(IMAGE_PROJECTION[2]))
                val orientation = it.getInt(it.getColumnIndexOrThrow(IMAGE_PROJECTION[4]))
                val path = it.getString(it.getColumnIndexOrThrow(IMAGE_PROJECTION[5]))
                val displayName = it.getString(it.getColumnIndexOrThrow(IMAGE_PROJECTION[6]))
                val dateAdded = it.getLong(it.getColumnIndexOrThrow(IMAGE_PROJECTION[7]))
                val size = it.getInt(it.getColumnIndexOrThrow(IMAGE_PROJECTION[8]))
                if (!fileExist(path) || TextUtils.isEmpty(displayName)) continue
                val media = MediaVo(
                        id, Uri.withAppendedPath(contentUri, id.toString()),
                        mimeType, dateTaken, dateModified.toInt(),
                        orientation, type.toLong(), path,
                        displayName, dateAdded, size
                )

                if (type == MediaType.VIDEO) {
                    val duration = it.getInt(it.getColumnIndexOrThrow(VIDEO_PROJECTION[9]))
                    media.duration = duration
                }

                data.add(media)
                // get all folder data
                val folderFile = File(path).parentFile
                if (folderFile != null && folderFile.exists()) {
                    val fp = folderFile.absolutePath
                    val f = getFolderByPath(fp, resultFolder)
                    if (f == null) {
                        val folder = FolderVo()
                        folder.name = folderFile.name
                        folder.path = fp
                        folder.cover = media
                        val mediaList = arrayListOf<MediaVo>()
                        mediaList.add(media)
                        folder.mediaStoreList = mediaList
                        resultFolder.add(folder)
                    } else {
                        f.mediaStoreList?.add(media)
                    }
                }
            }
        }
        return data
    }

    private fun fileExist(path: String): Boolean {
        return !TextUtils.isEmpty(path) && File(path).exists()
    }

    private fun getFolderByPath(path: String, resultFolder: MutableList<FolderVo>): FolderVo? {
        for (folder in resultFolder) {
            if (TextUtils.equals(folder.path, path)) {
                return folder
            }
        }
        return null
    }
}