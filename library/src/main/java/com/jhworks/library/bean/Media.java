package com.jhworks.library.bean;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * A data model containing data for a single media item.
 */
public class Media implements Parcelable {

    public final long rowId;
    public final Uri uri;
    public final String mimeType;
    public final long dateModified;
    public final int orientation;
    public final int type;
    public final long dateTaken;

    public final String path;
    public final String name;
    public final long time;
    public final int size;

    public boolean isSelect;

    public Media(long rowId, Uri uri, String mimeType, long dateTaken, long dateModified, int orientation,
                 int type, String path, String name, long time, int size) {
        this.rowId = rowId;
        this.uri = uri;
        this.mimeType = mimeType;
        this.dateModified = dateModified;
        this.orientation = orientation;
        this.type = type;
        this.dateTaken = dateTaken;
        this.path = path;
        this.name = name;
        this.time = time;
        this.size = size;
    }

    protected Media(Parcel in) {
        rowId = in.readLong();
        uri = in.readParcelable(Uri.class.getClassLoader());
        mimeType = in.readString();
        dateModified = in.readLong();
        orientation = in.readInt();
        type =in.readInt();
        dateTaken = in.readLong();
        path = in.readString();
        name = in.readString();
        time = in.readLong();
        size = in.readInt();
        isSelect = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(rowId);
        dest.writeParcelable(uri, flags);
        dest.writeString(mimeType);
        dest.writeLong(dateModified);
        dest.writeInt(orientation);
        dest.writeInt(type);
        dest.writeLong(dateTaken);
        dest.writeString(path);
        dest.writeString(name);
        dest.writeLong(time);
        dest.writeInt(size);
        dest.writeByte((byte) (isSelect ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Media> CREATOR = new Creator<Media>() {
        @Override
        public Media createFromParcel(Parcel in) {
            return new Media(in);
        }

        @Override
        public Media[] newArray(int size) {
            return new Media[size];
        }
    };

    @Override
    public String toString() {
        return "Media{" +
                "rowId=" + rowId +
                ", uri=" + uri +
                ", mimeType='" + mimeType + '\'' +
                ", dateModified=" + dateModified +
                ", orientation=" + orientation +
                ", type=" + type +
                ", dateTaken=" + dateTaken +
                ", path='" + path + '\'' +
                ", name='" + name + '\'' +
                ", time=" + time +
                ", size=" + size +
                '}';
    }

    /**
     * The type of data.
     */
    public interface Type {
        int VIDEO = 100;
        int IMAGE = 101;
    }
}
