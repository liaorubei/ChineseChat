package com.newclass.woyaoxue.database;

import com.newclass.woyaoxue.util.Log;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    private static String name = "woyaoxue.db";
    private static CursorFactory factory = null;
    private static int version = 1;

    public MySQLiteOpenHelper(Context context) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("MySQLiteOpenHelper onCreate");
        db.execSQL("create table document(Id int primary key,LevelId int,FolderId int,Title varchar,SoundPath varchar,IsDownload int,Json varchar);");
        db.execSQL("create table    level(Id int primary key,Name varchar);");
        db.execSQL("create table   folder(Id int primary key,Name varchar);");
        db.execSQL("create table UrlCache(Url varchar primary key,Json text,UpdateAt long);--请求缓存地址");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
