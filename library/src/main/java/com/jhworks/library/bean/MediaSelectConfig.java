package com.jhworks.library.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import com.jhworks.library.Constant;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * <p> 媒体配置</p>
 *
 * @author jiahui
 *         date 2018/1/9
 */
public class MediaSelectConfig implements Parcelable {
    // Single choice
    public static final int MODE_SINGLE = 0;
    // Multi choice
    public static final int MODE_MULTI = 1;

    @IntDef({MODE_SINGLE, MODE_MULTI})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SelectMode {
    }

    // image
    public static final int IMAGE = 10;
    // VIDEO
    public static final int VIDEO = 11;

    @IntDef({IMAGE, VIDEO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MediaType {
    }

    private boolean isShowCamera = false;
    private int maxCount = Constant.DEFAULT_IMAGE_SIZE;
    private int selectMode = MODE_MULTI;
    private ArrayList<String> originData;
    private int imageSpanCount = Constant.DEFAULT_IMAGE_SPAN_COUNT;
    private boolean openCameraOnly;
    private int mediaType;

    public MediaSelectConfig() {
    }

    protected MediaSelectConfig(Parcel in) {
        isShowCamera = in.readByte() != 0;
        maxCount = in.readInt();
        selectMode = in.readInt();
        originData = in.createStringArrayList();
        imageSpanCount = in.readInt();
        openCameraOnly = in.readByte() != 0;
        mediaType = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isShowCamera ? 1 : 0));
        dest.writeInt(maxCount);
        dest.writeInt(selectMode);
        dest.writeStringList(originData);
        dest.writeInt(imageSpanCount);
        dest.writeByte((byte) (openCameraOnly ? 1 : 0));
        dest.writeInt(mediaType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaSelectConfig> CREATOR = new Creator<MediaSelectConfig>() {
        @Override
        public MediaSelectConfig createFromParcel(Parcel in) {
            return new MediaSelectConfig(in);
        }

        @Override
        public MediaSelectConfig[] newArray(int size) {
            return new MediaSelectConfig[size];
        }
    };

    public MediaSelectConfig setShowCamera(boolean showCamera) {
        isShowCamera = showCamera;
        return this;
    }

    /**
     * set select multi count
     *
     * @param maxCount select multi count
     * @return -
     */
    public MediaSelectConfig setMaxCount(int maxCount) {
        this.maxCount = maxCount;
        return this;
    }

    /**
     * set select media mode
     *
     * @param selectMode 选择模式
     * @return -
     */
    public MediaSelectConfig setSelectMode(@SelectMode int selectMode) {
        this.selectMode = selectMode;
        return this;
    }

    /**
     * set origin image list resource
     *
     * @param originData origin image list resource
     * @return -
     */
    public MediaSelectConfig setOriginData(ArrayList<String> originData) {
        this.originData = originData;
        return this;
    }

    /**
     * image span count
     *
     * @param imageSpanCount span count ,default:4
     * @return -
     */
    public MediaSelectConfig setImageSpanCount(int imageSpanCount) {
        this.imageSpanCount = imageSpanCount;
        return this;
    }

    public MediaSelectConfig setOpenCameraOnly(boolean openCameraOnly) {
        this.openCameraOnly = openCameraOnly;
        return this;
    }

    public boolean isShowCamera() {
        return isShowCamera;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public int getSelectMode() {
        return selectMode;
    }

    public ArrayList<String> getOriginData() {
        return originData;
    }

    public int getImageSpanCount() {
        return imageSpanCount;
    }

    public boolean isOpenCameraOnly() {
        return openCameraOnly;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(@MediaType int mediaType) {
        this.mediaType = mediaType;
    }
}
