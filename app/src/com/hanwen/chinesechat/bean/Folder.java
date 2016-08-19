package com.hanwen.chinesechat.bean;

public class Folder {
    // {"Id":6,"Name":"舌尖上的中国","LevelId":1,"DocsCount":12}
    public int Id;
    public String Name;
    public int Sort;
    public int LevelId;
    public int DocsCount;
    public String Cover;

    @Override
    public String toString() {
        return "Folder [Id=" + Id + ", Name=" + Name + ", LevelId=" + LevelId + ", DocsCount=" + DocsCount + "]";
    }

}
