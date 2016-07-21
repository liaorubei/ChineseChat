package com.hanwen.chinesechat.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;

import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
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
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Log.i(TAG, "handleMessage: " + (NIMClient.getStatus()));

            HttpUtil.Parameters params = new HttpUtil.Parameters();
            params.add("Id", 205);
            HttpUtil.post(NetworkUtil.teacherEnqueue, params, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    Log.i(TAG, "onSuccess: " + responseInfo.result);
                }

                @Override
                public void onFailure(HttpException error, String msg) {
                    Log.i(TAG, "onFailure: error=" + error + " msg=" + msg);
                }
            });

            sendEmptyMessageDelayed(1, 60 * 1000);
        }
    };

    public ServiceQueue() {
        Log.i(TAG, "ServiceQueue: ");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: " + this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: ");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentText("ContentText");
        builder.setContentTitle("setContentTitle");
        builder.setSmallIcon(R.drawable.ic_launcher_student);
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE;
        startForeground(1, notification);

        handler.sendEmptyMessageDelayed(1, 60 * 1000);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind: ");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }
}
