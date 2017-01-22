package com.hanwen.chinesechat.service;

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
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.SparseArray;

import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.bean.Document;
import com.hanwen.chinesechat.bean.DownloadInfo;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

public class DownloadService extends Service {

    protected static final int NOTIFY = 0;
    private static final String TAG = "DownloadService";
    private MyBinder binder;
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
        return binder;
    }

    @Override
    public void onCreate() {
        binder = new MyBinder();
        manager = new DownloadManager();

        // 把数据库里面还没有下载完毕的任务取出来重新下载
        List<DownloadInfo> unfinishedDownload = ChineseChat.database().docsSelectUnfinishedDownload();
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
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

        public void enqueue(DownloadInfo info) {
            toDownload.add(info);

            //发送本地广播
            Intent intent = new Intent("音频下载");
            intent.putExtra("documentId", info.Id);
            intent.putExtra("current", 0);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
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
            Log.i(TAG, "onSuccess: 歌词下载完毕 " + responseInfo.result);
            ChineseChat.database().docsUpdateJson(this.mDocId, responseInfo.result);
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
        public void onStart() {

            DownloadInfo downloadInfo = manager.downloading.get(this.mDocId);
            builder = new NotificationCompat.Builder(DownloadService.this);
            builder.setSmallIcon(ChineseChat.isStudent() ? R.drawable.ic_launcher_student : R.drawable.ic_launcher_teacher);
            builder.setContentTitle(downloadInfo.Title);
            builder.setContentText("开始下载");
            builder.setProgress(100, 0, false);
            notificationManager.notify(this.mDocId, builder.build());
        }

        @Override
        public void onLoading(long total, long current, boolean isUploading) {
            builder.setContentText("正在下载");
            builder.setProgress((int) total, (int) current, false);
            notificationManager.notify(this.mDocId, builder.build());
            manager.change(this.mDocId, current, total);


            Intent intent = new Intent("音频下载");
            intent.putExtra("documentId", this.mDocId);
            int value = (int) (current * 100 / total);
            Log.i(TAG, "onLoading: " + value);
            intent.putExtra("current", value);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }

        @Override
        public void onSuccess(ResponseInfo<File> responseInfo) {
            Log.i(TAG, "onSuccess: 音频下载完毕 " + manager.downloading.get(this.mDocId).Title);
            // 下载成功之后要先移除下载列表里面的任务,并更新数据库,显示系统通知
            notificationManager.cancel(this.mDocId);

            // 移除并更新数据库
            manager.downloading.remove(this.mDocId);
            ChineseChat.database().docsUpdateDownloadStatusById(this.mDocId);

            // 通知观察者更新,让界面刷新
            manager.notifyObservers();

            //发送本地广播
            Intent intent = new Intent("音频下载");
            intent.putExtra("documentId", this.mDocId);
            intent.putExtra("current", 100);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }

        @Override
        public void onFailure(HttpException error, String msg) {
            // 下载失败了,通知下载失败并移除数据库里的数据
            builder.setContentText("下载失败");
            notificationManager.notify(this.mDocId, builder.build());
        }
    }

}
