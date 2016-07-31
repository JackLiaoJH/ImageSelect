package com.jhworks.library.bean;

import android.text.TextUtils;

import java.util.List;

/**
 * Author : LiaoJH
 * Desc : folder
 * Date: 2016/7/27
 */
public class Folder {
    public String name;
    public String path;
    public Media cover;
    public List<Media> mediaStoreList;

    @Override
    public boolean equals(Object o) {
        try {
            Folder other = (Folder) o;
            return TextUtils.equals(other.path, path);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return super.equals(o);
    }

    @Override
    public String toString() {
        return "Folder{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", cover=" + cover +
                ", mediaStoreList=" + mediaStoreList +
                '}';
    }
}
