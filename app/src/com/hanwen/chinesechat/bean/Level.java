package com.hanwen.chinesechat.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Level implements Parcelable {

    public int Id;
    public String Name;
    public int Sort;
    public int Show;
    public int ShowCover;
    public List<Folder> Folders;

    @Override
    public String toString() {
        return "Level{" +
                "Id=" + Id +
                ", Name='" + Name + '\'' +
                ", Sort=" + Sort +
                ", Show=" + Show +
                ", ShowCover=" + ShowCover +
                ", Folders=" + Folders +
                '}';
    }

    public static final Creator<Level> CREATOR = new Creator<Level>() {
        @Override
        public Level createFromParcel(Parcel in) {
            Level level = new Level();
            level.Id = in.readInt();
            level.Name = in.readString();
            level.Sort = in.readInt();
            level.Show = in.readInt();
            level.ShowCover = in.readInt();
            return level;
        }

        @Override
        public Level[] newArray(int size) {
            return new Level[size];
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
        dest.writeInt(Sort);
        dest.writeInt(Show);
        dest.writeInt(ShowCover);
    }
}
