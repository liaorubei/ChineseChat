package com.hanwen.chinesechat.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hanwen.chinesechat.bean.Document;
import com.hanwen.chinesechat.bean.DownloadInfo;
import com.hanwen.chinesechat.bean.Folder;
import com.hanwen.chinesechat.bean.Level;
import com.hanwen.chinesechat.bean.UrlCache;
import com.hanwen.chinesechat.bean.User;
import com.hanwen.chinesechat.util.Log;

import java.util.ArrayList;
import java.util.List;

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

    public void docsInsert(Document item) {
        ContentValues values = new ContentValues();
        values.put("Id", item.Id);
        values.put("TitleCn", item.Title);
        values.put("TitleEn", item.TitleEn);
        values.put("TitleSubCn", item.TitleSubCn);
        values.put("TitleSubEn", item.TitleSubEn);
        values.put("FolderId", item.FolderId);
        values.put("SoundPath", item.SoundPath);
        values.put("Category", item.Category);
        values.put("IsDownload", 0);
        Log.i(TAG, "docsInsert: insert=" + mWritable.insert("document", null, values));
    }

    public List<Document> docsSelectListByFolderId(int folderId) {
        Cursor cursor = mReadable.rawQuery("select Json from document where IsDownload=1 and FolderId=?", new String[]{folderId + ""});
        List<Document> list = new ArrayList<Document>();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        while (cursor.moveToNext()) {
            Document document = gson.fromJson(cursor.getString(0), Document.class);
            list.add(document);
        }
        cursor.close();
        return list;
    }

    public List<DownloadInfo> docsSelectUnfinishedDownload() {
        Cursor cursor = mReadable.rawQuery("select Id,TitleCn,SoundPath from document where IsDownload=0", null);
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

    public List<Folder> folderSelectList() {
        Cursor cursor = mReadable.rawQuery("select Id,Name,LevelId,Cover from folder;", null);
        List<Folder> list = new ArrayList<Folder>();
        while (cursor.moveToNext()) {
            Folder folder = new Folder();
            folder.Id = cursor.getInt(0);
            folder.Name = cursor.getString(1);
            folder.LevelId = cursor.getInt(2);
            folder.Cover = cursor.getString(3);
            list.add(folder);
        }
        cursor.close();
        return list;
    }

    /**
     * 查询已下载的文件的文件夹数,按Level排序,然后Folder排序
     *
     * @return select Id,Name,(select count(FolderId) from document where FolderId=folder.Id) as DocsCount from folder order by folder.Id
     */
    public List<Folder> folderSelectListWithDocsCount() {
        Cursor cursor = mReadable.rawQuery("select Id,Name,(select count(document.FolderId) From Document where Document.IsDownload=1 and Document.FolderId=Folder.Id) as DocsCount,Sort,Cover,LevelId From Folder where Folder.Id in (select distinct FolderId From Document)", null);
        List<Folder> list = new ArrayList<Folder>();
        while (cursor.moveToNext()) {
            Folder folder = new Folder();
            folder.Id = cursor.getInt(0);
            folder.Name = cursor.getString(1);
            folder.DocsCount = cursor.getInt(2);
            folder.Sort = cursor.getInt(3);
            folder.Cover = cursor.getString(4);
            folder.LevelId = cursor.getInt(5);
            list.add(folder);
        }
        cursor.close();
        return list;
    }

    public void levelInsertOrReplace(Level level) {
        ContentValues values = new ContentValues();
        values.put("Id", level.Id);
        values.put("Name", level.Name);
        values.put("Sort", level.Sort);
        values.put("ShowCover", level.ShowCover);
        mWritable.insertWithOnConflict("level", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public DownloadInfo docsSelectById(int id) {
        Cursor cursor = mReadable.rawQuery("select Id,Json,TitleCn,IsDownload from document where Id=?", new String[]{id + ""});
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
    }

    /**
     * 新添或者更新文件夹数据
     *
     * @param folder 实体
     */
    public void folderInsertOrReplace(Folder folder) {
        ContentValues value = new ContentValues();
        value.put("Id", folder.Id);
        value.put("Name", folder.Name);
        value.put("Sort", folder.Sort);
        value.put("Show", folder.Show);
        value.put("Cover", folder.Cover);
        value.put("LevelId", folder.LevelId);
        mWritable.insertWithOnConflict("folder", null, value, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void userInsertOrReplace(String username, String password) {
        ContentValues values = new ContentValues();
        values.put("Username", username);
        //values.put("Password", password);
        mWritable.insertWithOnConflict("User", "", values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<User> userList() {
        Cursor cursor = mReadable.query("User", new String[]{"Username", "Password"}, null, null, null, null, null);
        List<User> users = new ArrayList<>();
        while (cursor.moveToNext()) {
            User user = new User();
            user.Username = cursor.getString(0);
            user.PassWord = cursor.getString(1);
            users.add(user);
        }
        cursor.close();
        return users;
    }

    public Level levelGetByName(String name) {
        Cursor cursor = mReadable.query("Level", new String[]{"Id", "Name", "Sort"}, "Name=?", new String[]{name}, null, null, null);
        Level level = null;
        if (cursor.moveToNext()) {
            level = new Level();
            level.Id = cursor.getInt(0);
            level.Name = cursor.getString(1);
            level.Sort = cursor.getInt(2);
        }
        cursor.close();
        return level;
    }

    /**
     * 通过等级Id查询文件夹列表，查询课本列表
     *
     * @param levelId 课本等级
     * @return 0长度及以上的集合
     */
    public ArrayList<Folder> FolderListGetByLevelId(int levelId) {
        Cursor cursor = mReadable.query("Folder", new String[]{"Id", "Name", "Sort", "Cover"}, "LevelId=?", new String[]{levelId + ""}, null, null, "Sort");
        ArrayList<Folder> list = new ArrayList<Folder>();
        while (cursor.moveToNext()) {
            Folder folder = new Folder();
            folder.Id = cursor.getInt(0);
            folder.Name = cursor.getString(1);
            folder.Sort = cursor.getInt(2);
            folder.Cover = cursor.getString(3);
            folder.LevelId = levelId;
            list.add(folder);
        }
        cursor.close();
        return list;
    }

    public List<Document> docsGetListDownloaded() {
        List<Document> list = new ArrayList<>();
        Cursor cursor = mReadable.query("Document", new String[]{"Id", "TitleCn", "TitleEn", "TitleSubCn", "TitleSubEn", "Category", "SoundPath", "FolderId"}, "IsDownload=1", null, null, null, null);
        while (cursor.moveToNext()) {
            Document document = new Document();
            document.Id = cursor.getInt(0);
            document.TitleCn = cursor.getString(1);
            document.TitleEn = cursor.getString(2);
            document.TitleSubCn = cursor.getString(3);
            document.TitleSubEn = cursor.getString(4);
            document.Category = cursor.getInt(5);
            document.SoundPath = cursor.getString(6);
            document.FolderId = cursor.getInt(7);
            list.add(document);
        }
        cursor.close();
        return list;
    }

    public Folder folderGetById(Integer id) {
        Cursor cursor = mReadable.query("Folder", new String[]{"Id", "Name", "Sort", "Show", "Cover", "LevelId"}, "Id=?", new String[]{"" + id}, null, null, null);
        Folder folder = null;
        if (cursor.moveToNext()) {
            folder = new Folder();
            folder.Id = cursor.getInt(0);
            folder.Name = cursor.getString(1);
            folder.Sort = cursor.getInt(2);
            folder.Show = cursor.getInt(3);
            folder.Cover = cursor.getString(4);
            folder.LevelId = cursor.getInt(5);
        }
        cursor.close();
        return folder;
    }

    public List<Document> docsGetDownloadedListByFolderId(int folderId) {
        Cursor cursor = mReadable.query("Document", new String[]{"Id", "TitleCn", "TitleEn", "TitleSubCn", "TitleSubEn", "FolderId", "Category", "SoundPath"}, "FolderId=? and IsDownload=1", new String[]{"" + folderId}, null, null, "TitleCn");
        List<Document> documents = new ArrayList<>();
        while (cursor.moveToNext()) {
            Document document = new Document();
            document.Id = cursor.getInt(0);
            document.TitleCn = cursor.getString(1);
            document.TitleEn = cursor.getString(2);
            document.TitleSubCn = cursor.getString(3);
            document.TitleSubEn = cursor.getString(4);
            document.FolderId = cursor.getInt(5);
            document.Category = cursor.getInt(6);
            document.SoundPath = cursor.getString(7);
            documents.add(document);
        }
        cursor.close();
        return documents;
    }
}
