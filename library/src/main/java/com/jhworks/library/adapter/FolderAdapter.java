package com.jhworks.library.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.RequestManager;
import com.jhworks.library.R;
import com.jhworks.library.bean.Folder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件夹Adapter
 * Created by Nereo on 2015/4/7.
 * Updated by nereo on 2016/1/19.
 */
public class FolderAdapter extends BaseAdapter {

    private Context mContext;
    private RequestManager mRequestManager;
    private LayoutInflater mInflater;

    private List<Folder> mFolders = new ArrayList<>();

    int mImageSize;

    int lastSelected = 0;

    public FolderAdapter(Context context, RequestManager requestManager) {
        mContext = context;
        mRequestManager = requestManager;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageSize = mContext.getResources().getDimensionPixelOffset(R.dimen.mis_folder_cover_size);
    }

    /**
     * 设置数据集
     *
     * @param folders
     */
    public void setData(List<Folder> folders) {
        if (folders != null && folders.size() > 0) {
            mFolders = folders;
        } else {
            mFolders.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mFolders.size() + 1;
    }

    @Override
    public Folder getItem(int i) {
        if (i == 0) return null;
        return mFolders.get(i - 1);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.mis_list_item_folder, viewGroup, false);
            holder = new ViewHolder(view);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        if (holder != null) {
            if (i == 0) {
                holder.name.setText(R.string.mis_folder_all);
                holder.path.setText(R.string.sd_card);
                holder.size.setText(mContext.getResources().getString(R.string.mis_photo_unit, getTotalImageSize()));
                if (mFolders.size() > 0) {
                    Folder f = mFolders.get(0);
                    if (f != null) {
                        mRequestManager
                                .load(new File(f.cover.path))
                                .error(R.drawable.ic_photo_gray_63dp)
                                .override((int) mContext.getResources().getDimension(R.dimen.mis_folder_cover_size)
                                        , (int) mContext.getResources().getDimension(R.dimen.mis_folder_cover_size))
                                .centerCrop()
                                .into(holder.cover);
                    } else {
                        holder.cover.setImageResource(R.drawable.ic_photo_gray_63dp);
                    }
                }
            } else {
                holder.bindData(getItem(i));
            }
            if (lastSelected == i) {
                holder.indicator.setVisibility(View.VISIBLE);
            } else {
                holder.indicator.setVisibility(View.INVISIBLE);
            }
        }
        return view;
    }

    private int getTotalImageSize() {
        int result = 0;
        if (mFolders != null && mFolders.size() > 0) {
            for (Folder f : mFolders) {
                result += f.mediaStoreList.size();
            }
        }
        return result;
    }

    public void setSelectIndex(int i) {
        if (lastSelected == i) return;

        lastSelected = i;
        notifyDataSetChanged();
    }

    public int getSelectIndex() {
        return lastSelected;
    }

    class ViewHolder {
        ImageView cover;
        TextView name;
        TextView path;
        TextView size;
        ImageView indicator;

        ViewHolder(View view) {
            cover = (ImageView) view.findViewById(R.id.cover);
            name = (TextView) view.findViewById(R.id.name);
            path = (TextView) view.findViewById(R.id.path);
            size = (TextView) view.findViewById(R.id.size);
            indicator = (ImageView) view.findViewById(R.id.indicator);
            view.setTag(this);
        }

        void bindData(Folder data) {
            if (data == null) {
                return;
            }
            name.setText(data.name);
            path.setText(data.path);
            if (data.mediaStoreList != null) {
                size.setText(mContext.getResources().getString(R.string.mis_photo_unit, data.mediaStoreList.size()));
            } else {
                size.setText(mContext.getResources().getString(R.string.mis_no_photo_unit));
            }
            if (data.cover != null) {
                // 显示图片
                mRequestManager
                        .load(new File(data.cover.path))
                        .placeholder(R.drawable.ic_photo_gray_63dp)
                        .override((int) mContext.getResources().getDimension(R.dimen.mis_folder_cover_size)
                                , (int) mContext.getResources().getDimension(R.dimen.mis_folder_cover_size))
                        .centerCrop()
                        .into(cover);
            } else {
                cover.setImageResource(R.drawable.ic_photo_gray_63dp);
            }
        }
    }

}
