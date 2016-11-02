package com.hanwen.chinesechat.bean;


import java.util.List;

public class Hskk {
    public int Id;
    public int Rank;
    public String RankName;
    public int Part;
    public String PartName;
    public int Visible;
    public String Name;
    public String Desc;
    public List<HskkQuestion> Questions;
    public Integer Category;

    @Override
    public String toString() {
        return "Hskk{" +
                "Id=" + Id +
                ", Rank=" + Rank +
                ", Part=" + Part +
                ", Visible=" + Visible +
                ", Name='" + Name + '\'' +
                '}';
    }

}
