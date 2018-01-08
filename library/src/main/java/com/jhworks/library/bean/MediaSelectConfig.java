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
 * @date 2018/1/9
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

    public boolean isShowCamera = true;
    public int maxCount = Constant.DEFAULT_IMAGE_SIZE;
    public int selectMode = MODE_MULTI;
    public ArrayList<String> originData;
    public int imageSpanCount = Constant.DEFAULT_IMAGE_SPAN_COUNT;
    public boolean openCameraOnly;

    public MediaSelectConfig(){}

    protected MediaSelectConfig(Parcel in) {
        isShowCamera = in.readByte() != 0;
        maxCount = in.readInt();
        selectMode = in.readInt();
        originData = in.createStringArrayList();
        imageSpanCount = in.readInt();
        openCameraOnly = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isShowCamera ? 1 : 0));
        dest.writeInt(maxCount);
        dest.writeInt(selectMode);
        dest.writeStringList(originData);
        dest.writeInt(imageSpanCount);
        dest.writeByte((byte) (openCameraOnly ? 1 : 0));
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
     * @return
     */
    public MediaSelectConfig setMaxCount(int maxCount) {
        this.maxCount = maxCount;
        return this;
    }

    /**
     * set select media mode
     *
     * @return
     */
    public MediaSelectConfig setSelectMode(@SelectMode int selectMode) {
        this.selectMode = selectMode;
        return this;
    }

    /**
     * set origin image list resource
     *
     * @param originData origin image list resource
     * @return
     */
    public MediaSelectConfig setOriginData(ArrayList<String> originData) {
        this.originData = originData;
        return this;
    }

    /**
     * image span count
     *
     * @param imageSpanCount span count ,default:4
     * @return
     */
    public MediaSelectConfig setImageSpanCount(int imageSpanCount) {
        this.imageSpanCount = imageSpanCount;
        return this;
    }

    public MediaSelectConfig setOpenCameraOnly(boolean openCameraOnly) {
        this.openCameraOnly = openCameraOnly;
        return this;
    }
}
