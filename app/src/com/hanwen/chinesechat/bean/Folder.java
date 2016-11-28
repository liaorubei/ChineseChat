package com.hanwen.chinesechat.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Folder implements Parcelable {
    public int Id;
    public String Name;
    public int Sort;
    public int LevelId;
    public String Cover;
    public boolean Permission;
    public boolean HasChildren;
    public int KidsCount;
    public int DocsCount;
    public List<Folder> Children;
    public List<Document> Documents;

    public static final Creator<Folder> CREATOR = new Creator<Folder>() {
        @Override
        public Folder createFromParcel(Parcel in) {
            Folder folder = new Folder();
            folder.Id = in.readInt();
            folder.Name = in.readString();
            folder.Sort = in.readInt();
            folder.LevelId = in.readInt();
            folder.DocsCount = in.readInt();
            folder.Cover = in.readString();
            folder.Permission = in.readByte() != 0;
            return folder;
        }

        @Override
        public Folder[] newArray(int size) {
            return new Folder[size];
        }
    };
    public String NameEn;
    public String NameSubCn;
    public String NameSubEn;
    public int Show;

    @Override
    public String toString() {
        return "Folder{" +
                "Id=" + Id +
                ", Name='" + Name + '\'' +
                ", Sort=" + Sort +
                ", LevelId=" + LevelId +
                ", DocsCount=" + DocsCount +
                ", Cover='" + Cover + '\'' +
                ", Permission=" + Permission +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(Id);
        dest.writeString(Name);
        dest.writeInt(Sort);
        dest.writeInt(LevelId);
        dest.writeInt(DocsCount);
        dest.writeString(Cover);
        dest.writeByte((byte) (Permission ? 1 : 0));
    }
}
