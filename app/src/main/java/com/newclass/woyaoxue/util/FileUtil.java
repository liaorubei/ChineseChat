package com.newclass.woyaoxue.util;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;

import com.newclass.woyaoxue.ChineseChat;
import com.newclass.woyaoxue.database.Database;

import java.io.File;

/**
 * Created by 儒北 on 2016-03-31.
 */
public class FileUtil {
    private static final String TAG = "FileUtil";

    /**
     * 清除本应用内部缓存(/data/data/com.xxx.xxx/cache) * * @param context
     */
    public static void cleanInternalCache(Context context) {
        deleteFilesByDirectory(context.getCacheDir());
    }

    /**
     * 清除本应用所有数据库(/data/data/com.xxx.xxx/databases) * * @param context
     */
    public static void cleanDatabases(Context context) {
        File databases = new File(context.getFilesDir().getParent(), "databases");
        File[] files = databases.listFiles();
        for (File f : files) {
            Log.i(TAG, "cleanDatabases: " + f.getAbsolutePath());
            f.delete();
        }
    }

    /**
     * * 清除本应用SharedPreference(/data/data/com.xxx.xxx/shared_prefs) * * @param
     * context
     */
    public static void cleanSharedPreference(Context context) {
        deleteFilesByDirectory(new File("/data/data/" + context.getPackageName() + "/shared_prefs"));
    }

    /**
     * 清除/data/data/com.xxx.xxx/files下的内容 * * @param context
     */
    public static void cleanFiles(Context context) {
        deleteFilesByDirectory(context.getFilesDir());
        File[] files = context.getFilesDir().listFiles();
        for (File file : files) {
            fileDelete(file);
        }
    }

    public static void fileDelete(File file) {
        Log.i(TAG, "fileDelete: " + file.getAbsolutePath());
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                fileDelete(f);
            }
            file.delete();
        } else {
            file.delete();
        }
    }

    /**
     * * 清除外部cache下的内容(/mnt/sdcard/android/data/com.xxx.xxx/cache) * * @param
     * context
     */
    public static void cleanExternalCache(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            deleteFilesByDirectory(context.getExternalCacheDir());
        }
    }

    /**
     * 清除自定义路径下的文件，使用需小心，请不要误删。而且只支持目录下的文件删除 * * @param filePath
     */
    public static void cleanCustomCache(String filePath) {
        deleteFilesByDirectory(new File(filePath));
    }

    /**
     * 清除本应用所有的数据 * * @param context * @param filepath
     */
    public static void cleanApplicationData(Context context, String... filepath) {
        cleanInternalCache(context);
        cleanExternalCache(context);
        cleanDatabases(context);
        cleanSharedPreference(context);
        cleanFiles(context);
        for (String filePath : filepath) {
            cleanCustomCache(filePath);
        }
    }

    /**
     * 删除方法 这里只会删除某个文件夹下的文件，如果传入的directory是个文件，将不做处理 * * @param directory
     */
    private static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
    }

    public static long fileLength(File file) {
        long length = 0;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                length += fileLength(f);
            }
        } else {
            length = file.length();
        }
        return length;
    }

    public static void cleanMyDatabase() {
        Database database = ChineseChat.getDatabase();
        database.deleteTable("document");
        database.deleteTable("UrlCache");
    }
}
