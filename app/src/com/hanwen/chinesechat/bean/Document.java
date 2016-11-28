package com.hanwen.chinesechat.bean;

import java.util.Date;
import java.util.List;

public class Document {
    public int Id;
    public int LevelId;
    public int FolderId;
    public String Title;
    public String TitleTwo;
    public String TitleCn;
    public String TitleEn;
    public String TitlePy;
    public String TitleSubCn;
    public String TitleSubEn;
    public String TitleSubPy;
    public int Category;
    public List<Lyric> Lyrics;
    public String SoundPath;
    public double Duration;
    public long Length;
    public Date Date;
    public long Size;
    public Date AuditDate;
    /**
     * 播放时长
     */
    public String LengthString;
    public String DateString;
    public String Cover;
    public Folder Folder;

    public String toString() {
        return "Document{" +
                "Id=" + Id +
                ", FolderId=" + FolderId +
                ", TitleCn='" + TitleCn + '\'' +
                ", TitleEn='" + TitleEn + '\'' +
                ", TitleSubCn='" + TitleSubCn + '\'' +
                ", TitleSubEn='" + TitleSubEn + '\'' +
                ", Category=" + Category +
                ", SoundPath='" + SoundPath + '\'' +
                ", Cover='" + Cover + '\'' +
                '}';
    }
}
