package com.jhworks.library.load;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;

import com.jhworks.library.R;
import com.jhworks.library.bean.Folder;
import com.jhworks.library.bean.Media;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads metadata from the media store for images and videos.
 */
public class MediaDataLoader extends AsyncTaskLoader<List<Media>> {
    public static final String KEY_TYPE_CATEGORY = "path";

    private static final String[] IMAGE_PROJECTION =
            new String[]{
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.ImageColumns.DATE_TAKEN,
                    MediaStore.Images.ImageColumns.DATE_MODIFIED,
                    MediaStore.Images.ImageColumns.MIME_TYPE,
                    MediaStore.Images.ImageColumns.ORIENTATION,
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.DATE_ADDED,
                    MediaStore.Images.ImageColumns.SIZE,
            };

    private static final String[] VIDEO_PROJECTION =
            new String[]{
                    MediaStore.Video.VideoColumns._ID,
                    MediaStore.Video.VideoColumns.DATE_TAKEN,
                    MediaStore.Video.VideoColumns.DATE_MODIFIED,
                    MediaStore.Video.VideoColumns.MIME_TYPE,
                    "0 AS " + MediaStore.Images.ImageColumns.ORIENTATION,
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.DATE_ADDED,
                    MediaStore.Images.ImageColumns.SIZE,
            };

    private List<Media> cached;
    private boolean observerRegistered = false;
    private final ForceLoadContentObserver forceLoadContentObserver = new ForceLoadContentObserver();

    // folder result data set
    private ArrayList<Folder> mResultFolder = new ArrayList<>();

    private int mId;
    private Bundle mBundle;


    public MediaDataLoader(Context context, int id, Bundle bundle) {
        super(context);
        mId = id;
        mBundle = bundle;
    }

    @Override
    public void deliverResult(List<Media> data) {
        if (!isReset() && isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (cached != null) {
            deliverResult(cached);
        }
        if (takeContentChanged() || cached == null) {
            forceLoad();
        }
        registerContentObserver();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        cached = null;
        unregisterContentObserver();
    }

    @Override
    protected void onAbandon() {
        super.onAbandon();
        unregisterContentObserver();
    }

    public ArrayList<Folder> getResultFolder() {
        return mResultFolder;
    }

    @Override
    public List<Media> loadInBackground() {
        List<Media> data = queryImages();
//        data.addAll(queryVideos());
        /*Collections.sort(data, new Comparator<Media>() {
            @Override
            public int compare(Media mediaStoreData, Media mediaStoreData2) {
                return Long.valueOf(mediaStoreData2.dateTaken).compareTo(mediaStoreData.dateTaken);
            }
        });*/
        cached = data;
        return data;
    }

    private List<Media> queryImages() {
        StringBuilder selectAction = new StringBuilder();
        String[] selectArgs = null;
        if (mId == R.id.loader_all_media_store_data) {
            selectAction
                    .append(IMAGE_PROJECTION[8]).append(">0 AND ")
                    .append(IMAGE_PROJECTION[3]).append("=? OR ")
                    .append(IMAGE_PROJECTION[3]).append("=? ");
            selectArgs = new String[]{"image/jpeg", "image/png"};
        } else if (mId == R.id.loader_category_media_store_data) {
            selectAction
                    .append(IMAGE_PROJECTION[8]).append(">0 ");
            if (mBundle != null) {
                selectAction
                        .append(" AND ")
                        .append(IMAGE_PROJECTION[5]).append(" like '%")
                        .append(mBundle.getString(KEY_TYPE_CATEGORY)).append("%'");
            }
        }
        Cursor cursor = getContext().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                selectAction.toString(), selectArgs, IMAGE_PROJECTION[7] + " DESC");
        return query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor, Media.Type.IMAGE);
    }

    private List<Media> queryVideos() {
        Cursor cursor = getContext().getContentResolver()
                .query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, VIDEO_PROJECTION, null, null,
                        MediaStore.Video.VideoColumns.DATE_TAKEN + " DESC");
        return query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, cursor, Media.Type.VIDEO);
    }

    private List<Media> query(Uri contentUri, Cursor cursor,int type) {
        List<Media> data = new ArrayList<>();
        if (cursor == null) return data;
        try {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                long dateTaken = cursor.getLong(cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[3]));
                long dateModified = cursor.getLong(cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                int orientation = cursor.getInt(cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[4]));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[5]));
                String displayName = cursor.getString(cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[6]));
                long dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[7]));
                int size = cursor.getInt(cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[8]));

                if (!fileExist(path) || TextUtils.isEmpty(displayName)) continue;
                Media media = new Media(id, Uri.withAppendedPath(contentUri, Long.toString(id)),
                        mimeType, dateTaken, dateModified, orientation, type, path, displayName, dateAdded, size);
                data.add(media);


                // get all folder data
                File folderFile = new File(path).getParentFile();
                if (folderFile != null && folderFile.exists()) {
                    String fp = folderFile.getAbsolutePath();
                    Folder f = getFolderByPath(fp);
                    if (f == null) {
                        Folder folder = new Folder();
                        folder.name = folderFile.getName();
                        folder.path = fp;
                        folder.cover = media;
                        List<Media> mediaList = new ArrayList<>();
                        mediaList.add(media);
                        folder.mediaStoreList = mediaList;
                        mResultFolder.add(folder);
                    } else {
                        f.mediaStoreList.add(media);
                    }
                }
            }
        } finally {
            cursor.close();
        }
        return data;
    }

    private boolean fileExist(String path) {
        if (!TextUtils.isEmpty(path)) {
            return new File(path).exists();
        }
        return false;
    }

    private Folder getFolderByPath(String path) {
        if (mResultFolder != null) {
            for (Folder folder : mResultFolder) {
                if (TextUtils.equals(folder.path, path)) {
                    return folder;
                }
            }
        }
        return null;
    }

    private void registerContentObserver() {
        if (!observerRegistered) {
            ContentResolver cr = getContext().getContentResolver();
            cr.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false,
                    forceLoadContentObserver);
            cr.registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, false,
                    forceLoadContentObserver);

            observerRegistered = true;
        }
    }

    private void unregisterContentObserver() {
        if (observerRegistered) {
            observerRegistered = false;
            getContext().getContentResolver().unregisterContentObserver(forceLoadContentObserver);
        }
    }
}
