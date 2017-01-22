package com.hanwen.chinesechat.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ChineseChat on 2016/9/27.
 */
public class FolderDoc implements Parcelable {
    public boolean Permission;
    public int Id;
    public String Name1;
    public String Name2;
    public String Cover;
    public int Sort;
    public boolean isFolder;
    public boolean HasChildren;
    public boolean selected = false;
    public String Name;

    public FolderDoc() {
    }

    public FolderDoc(Folder f) {
        this.Id = f.Id;
        this.Name1 = f.Name;
        this.isFolder = true;
        this.Cover = f.Cover;
        this.Sort = f.Sort;
        this.Permission = true;
        this.HasChildren = f.KidsCount>0;
    }

    public FolderDoc(Parcel in) {
        Id = in.readInt();
        Name1 = in.readString();
        Sort = in.readInt();
        isFolder = in.readByte() != 0;
    }

    public static final Creator<FolderDoc> CREATOR = new Creator<FolderDoc>() {
        @Override
        public FolderDoc createFromParcel(Parcel in) {
            return new FolderDoc(in);
        }

        @Override
        public FolderDoc[] newArray(int size) {
            return new FolderDoc[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(Id);
        dest.writeString(Name1);
        dest.writeInt(Sort);
        dest.writeByte((byte) (isFolder ? 1 : 0));
    }
}
