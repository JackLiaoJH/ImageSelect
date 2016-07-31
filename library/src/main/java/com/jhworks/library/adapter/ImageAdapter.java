package com.jhworks.library.adapter;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.jhworks.library.R;
import com.jhworks.library.bean.Media;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * 图片Adapter
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageHolder> {

    private static final int TYPE_CAMERA = 0;
    private static final int TYPE_NORMAL = 1;

    private Context mContext;
    private RequestManager mRequestManager;
    private int mColumn;
    private LayoutInflater mInflater;
    private boolean showCamera = true;
    private boolean showSelectIndicator = true;

    private List<Media> mImages = new ArrayList<>();
    private List<Media> mSelectedImages = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;
    final int mGridWidth;
    private FrameLayout.LayoutParams mLayoutParams;
    private final int mSpaceSize;

    public ImageAdapter(Context context, RequestManager requestManager, boolean showCamera, int column) {
        mContext = context;
        mRequestManager = requestManager;
        mColumn = column;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.showCamera = showCamera;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            wm.getDefaultDisplay().getSize(size);
            width = size.x;
        } else {
            width = wm.getDefaultDisplay().getWidth();
        }
        mSpaceSize = (int) mContext.getResources().getDimension(R.dimen.mis_space_size);
        mGridWidth = (width - mSpaceSize * (2 + column - 1)) / column;
        mLayoutParams = new FrameLayout.LayoutParams(mGridWidth, mGridWidth);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    /**
     * 显示选择指示器
     *
     * @param b
     */
    public void showSelectIndicator(boolean b) {
        showSelectIndicator = b;
    }

    public void setShowCamera(boolean b) {
        if (showCamera == b) return;

        showCamera = b;
        notifyDataSetChanged();
    }

    public boolean isShowCamera() {
        return showCamera;
    }

    /**
     * 选择某个图片，改变选择状态
     *
     * @param media
     */
    public void select(Media media) {
        if (mSelectedImages.contains(media)) {
            mSelectedImages.remove(media);
        } else {
            mSelectedImages.add(media);
        }
        notifyDataSetChanged();
    }

    /**
     * 通过图片路径设置默认选择
     *
     * @param resultList
     */
    public void setDefaultSelected(ArrayList<String> resultList) {
        for (String path : resultList) {
            Media media = getImageByPath(path);
            if (media != null) {
                mSelectedImages.add(media);
            }
        }
        if (mSelectedImages.size() > 0) {
            notifyDataSetChanged();
        }
    }

    private Media getImageByPath(String path) {
        if (mImages != null && mImages.size() > 0) {
            for (Media Media : mImages) {
                if (Media.path.equalsIgnoreCase(path)) {
                    return Media;
                }
            }
        }
        return null;
    }

    /**
     * 设置数据集
     *
     * @param images
     */
    public void setData(List<Media> images) {
        mSelectedImages.clear();
        if (images != null && images.size() > 0) {
            mImages = images;
        } else {
            mImages.clear();
        }
        notifyDataSetChanged();
    }


    @Override
    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_CAMERA) {
            return new ImageHolder(mInflater.inflate(R.layout.mis_list_item_camera, parent, false));
        } else if (viewType == TYPE_NORMAL) {
            return new ImageHolder(mInflater.inflate(R.layout.mis_list_item_image, parent, false));
        }
        return new ImageHolder(new View(mContext));
    }

    @Override
    public void onBindViewHolder(ImageHolder holder, int position) {
        if (showCamera) {
            if (position != 0) {
                holder.image.setLayoutParams(mLayoutParams);
            } else {
                holder.itemView.setLayoutParams(mLayoutParams);
            }
        } else {
            holder.image.setLayoutParams(mLayoutParams);
        }
        bindData(getItem(position), holder, position);
    }

    @Override
    public int getItemViewType(int position) {
        if (showCamera) {
            return position == 0 ? TYPE_CAMERA : TYPE_NORMAL;
        }
        return TYPE_NORMAL;
    }

    public Media getItem(int i) {
        if (showCamera) {
            if (i == 0) {
                return null;
            }
            return mImages.get(i - 1);
        } else {
            return mImages.get(i);
        }
    }

    @Override
    public int getItemCount() {
        return showCamera ? mImages.size() + 1 : mImages.size();
    }

    private void bindData(final Media data, final ImageHolder holder, final int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onItemClick(data, position);
                }
            }
        });

        if (data == null) return;
        // 处理单选和多选状态
        if (showSelectIndicator) {
            holder.mCheckBox.setVisibility(View.VISIBLE);
            holder.mCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (null != mOnItemClickListener) {
                        mOnItemClickListener.onCheckClick(data, position);
                    }
                }
            });
            holder.mCheckBox.setButtonDrawable(mSelectedImages.contains(data) ? R.drawable.ic_check_circle_green_24dp :
                    R.drawable.ic_check_circle_while_24dp);
        } else {
            holder.mCheckBox.setVisibility(View.GONE);
        }
        File imageFile = new File(data.path);
        if (imageFile.exists()) {
            mRequestManager
                    .load(imageFile)
                    .placeholder(R.drawable.ic_photo_gray_63dp)
                    .override(mGridWidth, mGridWidth)
                    .centerCrop()
                    .crossFade(300)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.ic_photo_gray_63dp);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Media media, int position);

        void onCheckClick(Media media, int position);
    }

    public static class ImageHolder extends RecyclerView.ViewHolder {
        ImageView image;
        AppCompatCheckBox mCheckBox;

        public ImageHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            mCheckBox = (AppCompatCheckBox) itemView.findViewById(R.id.checkmark);
            itemView.setTag(this);
        }
    }
}
