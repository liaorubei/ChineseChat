package com.hanwen.chinesechat.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;

import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.activity.ActivityMain;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.util.SoundPlayer;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.model.AVChatData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ServiceQueue extends Service {
    private static final String TAG = "ServiceQueue";
    private static final int WHAT_REFRESH = 1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage: " + (NIMClient.getStatus()));
            HttpUtil.Parameters params = new HttpUtil.Parameters();
            params.add("Id", ChineseChat.CurrentUser.Id);
            params.add("system", 1);//刷新参数,表明是那个系统的客户端,安卓为1,苹果为2,其他为0
            params.add("device", Build.DEVICE);
            HttpUtil.post(NetworkUtil.teacherRefresh, params, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    Log.i(TAG, "onSuccess: " + responseInfo.result);
                }

                @Override
                public void onFailure(HttpException error, String msg) {
                    Log.i(TAG, "onFailure: error=" + error + " msg=" + msg);
                }
            });
            sendEmptyMessageDelayed(WHAT_REFRESH, 60 * 1000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: " + this);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_teacher));
        builder.setSmallIcon(R.drawable.ic_launcher_student);
        builder.setContentTitle("你正在排队当中！");
        builder.setContentText("汉问教师端队列辅助提示");
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setOngoing(true).setAutoCancel(false).setTicker("你正在排队中！").setWhen(System.currentTimeMillis());
        Intent intent = new Intent(this, ActivityMain.class);
        intent.putExtra(ActivityMain.KEY_TAB_INDEX, 0);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE | Notification.FLAG_NO_CLEAR;
        startForeground(10, notification);
        handler.sendEmptyMessageDelayed(WHAT_REFRESH, 60 * 1000);
    }

/*    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: " + this);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentText("ContentText");
        builder.setContentTitle("setContentTitle");
        builder.setSmallIcon(R.drawable.ic_launcher_student);
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE;
        //startForeground(1, notification);

        handler.removeCallbacksAndMessages(null);
        handler.sendEmptyMessageDelayed(1, 60 * 1000);

        return super.onStartCommand(intent, flags, startId);
    }*/

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind: ");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
        handler.removeMessages(WHAT_REFRESH);
    }
}
