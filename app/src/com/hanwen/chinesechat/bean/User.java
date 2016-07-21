package com.hanwen.chinesechat.bean;

import java.util.Set;

public class User {
    public int Id;
    public String Token;
    public String Accid;
    public String Nickname;
    public String Username;
    public String PassWord;
    public String Icon;
    public String Avatar;
    public int Gender = -1;
    public int Category;
    public String Email;
    public String Birth;
    public String Mobile;
    public int Coins;
    public double Score;

    public String About;
    public String Voice;
    public String Country;
    public String Language;
    public String Job;
    public boolean IsEnable;
    public boolean IsOnline;
    public Set<String> Photos;
    public String School;
    public String Spoken;
    public String Hobbies;

    public Summary Summary = new Summary();

    @Override
    public String toString() {
        return "User{" +
                "Id=" + Id +
                ", Token='" + Token + '\'' +
                ", Accid='" + Accid + '\'' +
                ", Nickname='" + Nickname + '\'' +
                ", Username='" + Username + '\'' +
                ", PassWord='" + PassWord + '\'' +
                ", Icon='" + Icon + '\'' +
                ", Avatar='" + Avatar + '\'' +
                ", Gender=" + Gender +
                ", Category=" + Category +
                ", Email='" + Email + '\'' +
                ", Birth='" + Birth + '\'' +
                ", Mobile='" + Mobile + '\'' +
                ", Coins=" + Coins +
                ", Score=" + Score +
                ", About='" + About + '\'' +
                ", Voice='" + Voice + '\'' +
                ", Country='" + Country + '\'' +
                ", Language='" + Language + '\'' +
                ", Job='" + Job + '\'' +
                ", IsEnable=" + IsEnable +
                ", IsOnline=" + IsOnline +
                ", Photos=" + Photos +
                ", School='" + School + '\'' +
                ", Spoken='" + Spoken + '\'' +
                ", Hobbies='" + Hobbies + '\'' +
                ", Summary=" + Summary +
                '}';
    }
}
