package com.jhworks.library.bean;

import java.util.ArrayList;

/**
 * <p>全局存数据  </p>
 *
 * @author jiahui
 * @date 2018/1/17
 */
public class DataBundle {
    private ArrayList<Media> mMediaList;

    private static class DataBundleHelper {
        private static volatile DataBundle INSTANCE = new DataBundle();
    }

    public static DataBundle get() {
        return DataBundleHelper.INSTANCE;
    }

    public void setMediaList(ArrayList<Media> mediaList) {
        this.mMediaList = mediaList;
    }

    public ArrayList<Media> getMediaList() {
        return mMediaList;
    }
}
