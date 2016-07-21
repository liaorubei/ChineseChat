package com.hanwen.chinesechat.service;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.TextUtils;
import android.util.SparseArray;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.hanwen.chinesechat.bean.DownloadInfo;
import com.hanwen.chinesechat.database.Database;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.R;

public class DownloadService extends Service {

    protected static final int NOTIFY = 0;
    private static final String TAG = "DownloadService";
    private MyBinder binder;
    private Database database;
    private int downloadCount = 5;// 最多下载数
    private DownloadManager manager;

    private NotificationManager notificationManager;

    private static Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case NOTIFY:
                    ((Observable) msg.obj).notifyObservers();
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("DownloadService--onBind");
        return binder;
    }

    @Override
    public void onCreate() {
        binder = new MyBinder();
        manager = new DownloadManager();
        database = new Database(this);

        // 把数据库里面还没有下载完毕的任务取出来重新下载
        List<DownloadInfo> unfinishedDownload = database.docsSelectUnfinishedDownload();
        manager.toDownload.addAll(unfinishedDownload);

        // 通知管理器
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    if (manager.toDownload.size() > 0) {
                        DownloadInfo info = manager.toDownload.remove();
                        manager.downloading.put(info.Id, info);

                        if (!TextUtils.isEmpty(info.SoundPath)) {
                            // 下载音频文件
                            new HttpUtils().download(NetworkUtil.getFullPath(info.SoundPath), new File(getFilesDir(), info.SoundPath).getAbsolutePath(), new MyAudioCallBack(info.Id));
                            // 下载歌词文件
                            new HttpUtils().send(HttpMethod.GET, NetworkUtil.getDocById(info.Id), new MyLyricCallBack(info.Id));
                        }
                    }

                    if (manager.size() > 0) {
                        Message message = handler.obtainMessage();
                        message.obj = manager;
                        message.what = NOTIFY;
                        handler.sendMessage(message);
                    }
                    SystemClock.sleep(1000);
                }
            }
        }).start();

    }

    @Override
    public void onDestroy() {
        database.closeConnection();
    }

    public class DownloadManager extends Observable {
        private SparseArray<DownloadInfo> downloading;// 正在下载
        private LinkedList<DownloadInfo> toDownload;// 准备下载

        DownloadManager() {
            toDownload = new LinkedList<DownloadInfo>();
            downloading = new SparseArray<DownloadInfo>();
        }

        public void change(int key, long current, long total) {
            DownloadInfo down = this.downloading.get(key);
            down.Current = current;
            down.Total = total;

            // 标识数据已经发生了改变
            setChanged();
        }

        public void enqueue(DownloadInfo path) {
            toDownload.add(path);
        }

        public DownloadInfo get(int key) {
            DownloadInfo downloadInfo = this.downloading.get(key);
            if (downloadInfo == null) {
                for (DownloadInfo i : toDownload) {
                    if (i.Id == key) {
                        downloadInfo = i;
                    }
                }
            }
            return downloadInfo;
        }

        public int size() {
            return this.toDownload.size() + downloading.size();
        }
    }

    public class MyBinder extends Binder {

        public DownloadManager getDownloadManager() {
            return manager;
        }

    }

    private class MyLyricCallBack extends RequestCallBack<String> {

        private int mDocId;

        public MyLyricCallBack(int id) {
            this.mDocId = id;
        }

        @Override
        public void onSuccess(ResponseInfo<String> responseInfo) {
            Log.i("歌词信息下载完毕");
            database.docsUpdateJson(this.mDocId, responseInfo.result);
        }

        @Override
        public void onFailure(HttpException error, String msg) {
        }
    }

    private class MyAudioCallBack extends RequestCallBack<File> {
        private Builder builder;
        private int mDocId;

        public MyAudioCallBack(int id) {
            this.mDocId = id;
        }

        @Override
        public void onFailure(HttpException error, String msg) {
            // 下载失败了,通知下载失败并移除数据库里的数据
            builder.setContentText("下载失败");
            notificationManager.notify(this.mDocId, builder.build());
        }

        public void onLoading(long total, long current, boolean isUploading) {
            builder.setContentText("正在下载");
            builder.setProgress((int) total, (int) current, false);
            notificationManager.notify(this.mDocId, builder.build());

            manager.change(this.mDocId, current, total);
        }

        public void onStart() {
            builder = new NotificationCompat.Builder(DownloadService.this);
            builder.setSmallIcon(R.drawable.ic_launcher);
            builder.setContentTitle(manager.downloading.get(this.mDocId).Title);
            builder.setContentText("开始下载");
            builder.setProgress(100, 0, false);
            notificationManager.notify(this.mDocId, builder.build());
        }

        @Override
        public void onSuccess(ResponseInfo<File> responseInfo) {
            // 下载成功之后要先移除下载列表里面的任务,并更新数据库,显示系统通知
            notificationManager.cancel(this.mDocId);

            // 移除并更新数据库
            manager.downloading.remove(this.mDocId);
            database.docsUpdateDownloadStatusById(this.mDocId);

            // 通知观察者更新,让界面刷新
            manager.notifyObservers();
        }
    }

}
