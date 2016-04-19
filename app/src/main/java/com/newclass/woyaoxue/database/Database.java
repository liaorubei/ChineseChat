package com.newclass.woyaoxue.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.google.gson.Gson;
import com.newclass.woyaoxue.bean.Document;
import com.newclass.woyaoxue.bean.DownloadInfo;
import com.newclass.woyaoxue.bean.Folder;
import com.newclass.woyaoxue.bean.Level;
import com.newclass.woyaoxue.bean.UrlCache;
import com.newclass.woyaoxue.util.Log;

public class Database {

    private static final String TAG = "Database";
    private MySQLiteOpenHelper helper;
    private SQLiteDatabase mReadable;
    private SQLiteDatabase mWritable;

    public Database(Context context) {
        helper = new MySQLiteOpenHelper(context);
        mReadable = helper.getReadableDatabase();
        mWritable = helper.getWritableDatabase();
    }

    public void cacheInsertOrUpdate(UrlCache urlCache) {
        Cursor cursor = mReadable.rawQuery("Select Url From UrlCache where Url=?", new String[]{urlCache.Url});
        ContentValues values = new ContentValues();
        values.put("Json", urlCache.Json);
        values.put("UpdateAt", urlCache.UpdateAt);
        if (cursor.getCount() > 0) {
            mWritable.update("UrlCache", values, "Url=?", new String[]{urlCache.Url});
        } else {
            values.put("Url", urlCache.Url);
            mWritable.insert("UrlCache", null, values);
        }
        cursor.close();
    }

    public UrlCache cacheSelectByUrl(String url) {
        Cursor cursor = mReadable.rawQuery("select Url,Json,UpdateAt from UrlCache where Url=?", new String[]{url});
        UrlCache cache = null;
        if (cursor.moveToNext()) {
            cache = new UrlCache();
            cache.Url = cursor.getString(0);
            cache.Json = cursor.getString(1);
            cache.UpdateAt = cursor.getLong(2);
        }
        cursor.close();
        return cache;
    }

    /**
     * 请及时关闭数据库连接,否则容易出现内存泄漏,数据库连接是比较耗内存的资源
     */
    public void closeConnection() {
        helper.close();
    }

    public int docsCountByFolderId(int folderId) {
        Cursor cursor = mReadable.rawQuery("select count(FolderId) from document where FolderId=?", new String[]{folderId + ""});
        int count = 0;
        if (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }
        return count;
    }

    public void docsDeleteById(int id) {
        mWritable.delete("document", "Id=?", new String[]{id + ""});
    }

    /**
     * @param id
     * @return 数据库是否有这条记录的判断, 如果有则true, 反之false
     */
    public boolean docsExists(int id) {
        Cursor cursor = mReadable.rawQuery("select Id from document where Id=?", new String[]{id + ""});
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public void docsInsert(Document item) {
        ContentValues values = new ContentValues();

        values.put("Id", item.Id);
        values.put("Title", item.Title);
        values.put("LevelId", item.LevelId);
        values.put("FolderId", item.FolderId);
        values.put("SoundPath", item.SoundPath);
        values.put("IsDownload", 0);
        Log.i(TAG, "docsInsert: insert=" + mWritable.insert("document", null, values));

    }

    public List<Document> docsSelectListByFolderId(Integer folderId) {
        Cursor cursor = mReadable.rawQuery("select Json from document where IsDownload=1 and FolderId=?", new String[]{folderId + ""});
        List<Document> list = new ArrayList<Document>();
        Gson gson = new Gson();
        while (cursor.moveToNext()) {
            Document document = gson.fromJson(cursor.getString(0), Document.class);
            list.add(document);
        }
        cursor.close();
        return list;
    }

    public List<DownloadInfo> docsSelectUnfinishedDownload() {
        Cursor cursor = mReadable.rawQuery("select Id,Title,SoundPath from document where IsDownload=0", null);
        List<DownloadInfo> infos = new ArrayList<DownloadInfo>();
        while (cursor.moveToNext()) {
            DownloadInfo info = new DownloadInfo();
            info.Id = cursor.getInt(0);
            info.Title = cursor.getString(1);
            info.SoundPath = cursor.getString(2);

            infos.add(info);
        }
        cursor.close();
        return infos;
    }

    public void docsUpdateDownloadStatusById(int id) {
        mWritable.execSQL("update document set IsDownload=1 where Id=?", new String[]{id + ""});
    }

    public void docsUpdateJson(int docId, String result) {
        mWritable.execSQL("update document set Json=? where Id=?", new String[]{result, docId + ""});
    }

    public boolean folderExists(int id) {
        Cursor cursor = mReadable.rawQuery("select Id from folder where Id=?", new String[]{id + ""});
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public void folderInsert(Folder folder) {
        ContentValues values = new ContentValues();
        values.put("Id", folder.Id);
        values.put("Name", folder.Name);
        mWritable.insert("folder", null, values);
    }

    public List<Folder> folderSelectList() {
        Cursor cursor = mReadable.rawQuery("select Id,Name from folder", null);
        List<Folder> list = new ArrayList<Folder>();
        while (cursor.moveToNext()) {
            Folder folder = new Folder();
            folder.Id = cursor.getInt(0);
            folder.Name = cursor.getString(1);
            list.add(folder);
        }
        return list;
    }

    /**
     * @return select Id,Name,(select count(FolderId) from document where FolderId=folder.Id) as DocsCount from folder order by folder.Id
     */
    public List<Folder> folderSelectListWithDocsCount() {
        Cursor cursor = mReadable.rawQuery("select Id,Name,(select count(FolderId) from document where FolderId=folder.Id) as DocsCount from folder order by folder.Id", null);
        List<Folder> list = new ArrayList<Folder>();
        while (cursor.moveToNext()) {
            Folder folder = new Folder();
            folder.Id = cursor.getInt(0);
            folder.Name = cursor.getString(1);
            folder.DocsCount = cursor.getInt(2);
            list.add(folder);
        }
        return list;
    }

    public void levelClear() {
    }

    public boolean levelExists(int id) {
        Cursor cursor = mReadable.rawQuery("select Id from level where Id=?", new String[]{id + ""});
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public void levelInsert(Level level) {
        ContentValues values = new ContentValues();
        values.put("Id", level.Id);
        values.put("Name", level.Name);
        mWritable.insert("level", null, values);
    }

    public DownloadInfo docsSelectById(int id) {
        Cursor cursor = mReadable.rawQuery("select Id,Json,Title,IsDownload from document where Id=?", new String[]{id + ""});
        DownloadInfo info = null;
        if (cursor.moveToNext()) {
            info = new DownloadInfo();
            info.Id = cursor.getInt(0);
            info.Json = cursor.getString(1);
            info.Title = cursor.getString(2);
            info.IsDownload = cursor.getInt(3);
        }
        cursor.close();
        return info;
    }

    public void deleteTable(String table) {
        Log.i(TAG, "deleteTable: " + mWritable.delete(table, null, null));
        ;
    }
}
