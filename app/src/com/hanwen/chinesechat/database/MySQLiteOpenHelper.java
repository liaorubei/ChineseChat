package com.hanwen.chinesechat.database;

import com.hanwen.chinesechat.util.Log;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    private static String name = "woyaoxue.db";
    private static CursorFactory factory = null;
    private static int version = 9;

    public MySQLiteOpenHelper(Context context) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("MySQLiteOpenHelper onCreate");
        /*
        db.execSQL("create table document(Id int primary key,LevelId int,FolderId int,Title varchar,SoundPath varchar,IsDownload int,Category int,Json varchar);");

        //db.execSQL("create table    level(Id int primary key,Name varchar);");
        //db.execSQL("create table   folder(Id int primary key,Name varchar);");--版本1

        //db.execSQL("create table folder(Id int primary key,Name varchar,LevelId int,Cover varchar)");// 版本2

        //版本3
        db.execSQL("CREATE TABLE Level( Id int PRIMARY KEY,Name varchar,Sort int,ShowCover int)");
        db.execSQL("CREATE TABLE Folder(Id int PRIMARY KEY,Name varchar,Sort int,Cover varchar,LevelId int)");
        db.execSQL("CREATE TABLE UrlCache(Url varchar primary key,Json text,UpdateAt long);--请求缓存地址");

        //版本5，添加一个本地用户登录的记录
        db.execSQL("CREATE TABLE User(Username VARCHAR PRIMARY KEY,password VARCHAR)");
        */
        //-----------------------------------------第5版本之后------------------------------------------------
        db.execSQL("CREATE TABLE User     (Username nvarchar primary key,Password nvarchar)");
        db.execSQL("CREATE TABLE Folder   (Id int primary key,Name nvarchar,Sort int,Show int,Cover nvarchar,LevelId int)");
        db.execSQL("CREATE TABLE Document (Id int primary key,TitleCn nvarchar,TitleEn nvarchar,TitleSubCn nvarchar,TitleSubEn nvarchar,SoundPath nvarchar,IsDownload int,IsNew int,Category int,Json nvarchar,FolderId int,Length float,Duration float,AuditDate nvarchar)");
        db.execSQL("CREATE TABLE UrlCache (Url nvarchar primary key,Json text,UpdateAt long)");
        //话题表
        db.execSQL("create table Theme    (Id int primary key,NameCn nvarchar,NameEn nvarchar,Sort int)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE if exists Level");
        db.execSQL("DROP TABLE if exists Folder");
        db.execSQL("DROP TABLE if exists Document");
        db.execSQL("DROP TABLE if exists User");
        db.execSQL("DROP TABLE if exists UrlCache");
        db.execSQL("drop table if exists Theme");

        db.execSQL("CREATE TABLE User     (Username nvarchar primary key,Password nvarchar)");
        db.execSQL("CREATE TABLE Folder   (Id int primary key,Name nvarchar,Sort int,Show int,Cover nvarchar,LevelId int)");
        db.execSQL("CREATE TABLE Document (Id int primary key,TitleCn nvarchar,TitleEn nvarchar,TitleSubCn nvarchar,TitleSubEn nvarchar,SoundPath nvarchar,IsDownload int,IsNew int,Category int,Json nvarchar,FolderId int,Length float,Duration float,AuditDate nvarchar)");

        db.execSQL("CREATE TABLE UrlCache (Url nvarchar primary key,Json text,UpdateAt long)");
        db.execSQL("create table Theme    (Id int primary key,NameCn nvarchar,NameEn nvarchar,Sort int)");
    }

}
