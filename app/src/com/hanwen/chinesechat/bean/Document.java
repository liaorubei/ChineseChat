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

    //客户端辅助相关,int32位4字节，因为Cursor取不出来1个字节的整数，只有2，3，4个字节的整数，分别是Short，Integer，Long，所以这是取了一个Integer做布尔值
    public int IsDownload;
    public int IsNew;

    @Override
    public String toString() {
        return "Document{" +
                "Title='" + Title + '\'' +
                ", TitleTwo='" + TitleTwo + '\'' +
                ", Id=" + Id +
                ", LevelId=" + LevelId +
                ", FolderId=" + FolderId +
                ", TitleCn='" + TitleCn + '\'' +
                ", TitleEn='" + TitleEn + '\'' +
                ", TitlePy='" + TitlePy + '\'' +
                ", TitleSubCn='" + TitleSubCn + '\'' +
                ", TitleSubEn='" + TitleSubEn + '\'' +
                ", TitleSubPy='" + TitleSubPy + '\'' +
                ", Category=" + Category +
                ", SoundPath='" + SoundPath + '\'' +
                ", Cover='" + Cover + '\'' +
                '}';
    }
}
