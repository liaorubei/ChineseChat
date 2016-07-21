package com.hanwen.chinesechat.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;

//教师保持在线自动刷新服务,与教师排队界面的自动刷新服务相冲突，如果排队界面是visable，那么自动刷新服务不发送请求，自动刷新服务每4分钟刷新一次
public class TeacherAutoRefreshService extends Service {
    private static final String TAG = "TeacherAutoRefreshService";
    private static final int REFRESH_DATA = 1;
    private static int time = 0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case REFRESH_DATA:
                    int offset = 10;//回调间隔，秒
                    time += offset;//自增30秒
                    Log.i(TAG, "handleMessage: time=" + time);
                    if (time >= 300) {
                        requestData();
                        time = 0;
                    }
                    sendEmptyMessageDelayed(REFRESH_DATA, offset * 1000);//30秒回调一次，4分钟刷新一次
                    break;
            }
        }
    };

    private void requestData() {
        HttpUtil.Parameters params = new HttpUtil.Parameters();
        params.add("id", ChineseChat.CurrentUser.Id);
        HttpUtil.post(NetworkUtil.teacherRefresh, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: " + msg);
                time=300;
            }
        });
    }


    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: ");
        handler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
    }
}
