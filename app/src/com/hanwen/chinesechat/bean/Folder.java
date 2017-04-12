package com.hanwen.chinesechat.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Folder implements Parcelable {
    public int Id;
    public String Name;
    public String NameEn;
    public String NameSubCn;
    public String NameSubEn;
    public int Show;
    public int Sort;
    public int LevelId;
    public String Cover;
    public int KidsCount;
    public int DocsCount;
    public int NewsCount;//辅助字段，用于Sqlite
    public List<Folder> Children;
    public List<Document> Documents;
    public int ParentId;
    public int TargetId;

    @Override
    public String toString() {
        return "Folder{" +
                "Id=" + Id +
                ", Name='" + Name + '\'' +
                ", NameEn='" + NameEn + '\'' +
                ", NameSubCn='" + NameSubCn + '\'' +
                ", NameSubEn='" + NameSubEn + '\'' +
                ", Show=" + Show +
                ", Sort=" + Sort +
                ", ParentId=" + ParentId +
                ", TargetId=" + TargetId +
                ", Cover='" + Cover + '\'' +
                ", DocsCount=" + DocsCount +
                ", KidsCount=" + KidsCount +
                ", NewCount=" + NewsCount +
                ", LevelId=" + LevelId +
                '}';
    }

    public static final Creator<Folder> CREATOR = new Creator<Folder>() {
        @Override
        public Folder createFromParcel(Parcel in) {
            Folder folder = new Folder();
            folder.Id = in.readInt();
            folder.Name = in.readString();
            folder.NameEn = in.readString();
            folder.NameSubCn = in.readString();
            folder.NameSubEn = in.readString();
            folder.Show = in.readInt();
            folder.Sort = in.readInt();
            folder.LevelId = in.readInt();
            folder.Cover = in.readString();
            folder.KidsCount = in.readInt();
            folder.DocsCount = in.readInt();
            folder.ParentId = in.readInt();
            folder.TargetId = in.readInt();
            return folder;
        }

        @Override
        public Folder[] newArray(int size) {
            return new Folder[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(Id);
        dest.writeString(Name);
        dest.writeString(NameEn);
        dest.writeString(NameSubCn);
        dest.writeString(NameSubEn);
        dest.writeInt(Show);
        dest.writeInt(Sort);
        dest.writeInt(LevelId);
        dest.writeString(Cover);
        dest.writeInt(KidsCount);
        dest.writeInt(DocsCount);
        dest.writeInt(ParentId);
        dest.writeInt(TargetId);
    }
}
