package com.hanwen.chinesechat.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//教师保持在线自动刷新服务,与教师排队界面的自动刷新服务相冲突
public class TeacherAutoRefreshService extends Service {
    private static final String TAG = "TeacherAutoRefreshService";
    private static final int WHAT_REFRESH = 1;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == WHAT_REFRESH) {
                Log.i(TAG, "handleMessage: " + NIMClient.getStatus());
                HttpUtil.Parameters params = new HttpUtil.Parameters();
                params.add("id", ChineseChat.CurrentUser.Id);
                params.add("isOnline", NIMClient.getStatus() == StatusCode.LOGINED ? 1 : 0);
                HttpUtil.post(NetworkUtil.teacherRefresh, params, null);

                IMMessage message = MessageBuilder.createTextMessage("1", SessionTypeEnum.P2P, "1");// MessageBuilder.createEmptyMessage("12315", SessionTypeEnum.P2P,0);
                NIMClient.getService(MsgService.class).sendMessage(message, false).setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "onSuccess: ");
                    }

                    @Override
                    public void onFailed(int i) {
                        Log.i(TAG, "onFailed: " + i);
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        Log.i(TAG, "onException: " + throwable.getMessage());
                    }
                });

                RequestBody body = new FormBody.Builder().add("id", ChineseChat.CurrentUser.Id + "").add("isOnline", "1").build();
                Request request = new Request.Builder().url(NetworkUtil.teacherRefresh).post(body).build();
                new OkHttpClient.Builder().build().newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.i(TAG, "onResponse: " + response);
                    }
                });
                sendEmptyMessageDelayed(WHAT_REFRESH, 2 * 60 * 1000);
            }
        }
    };
    private BroadcastReceiver receiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
        handler.sendEmptyMessageDelayed(WHAT_REFRESH, 0);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager systemService = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                boolean availableMobile = systemService.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED;
                boolean availableWifi = systemService.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
                Log.i(TAG, "onReceive: availableMobile=" + availableMobile + " availableWifi=" + availableWifi);
            }
        };

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "onStartCommand: " + intent);

        //START_STICKY：如果service进程被kill掉，保留service的状态为开始状态，但不保留递送的intent对象。随后系统会尝试重新创建service，由于服务状态为开始状态，所以创建服务后一定会调用onStartCommand(Intent,int,int)方法。如果在此期间没有任何启动命令被传递到service，那么参数Intent将为null。
        //START_NOT_STICKY：“非粘性的”。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统不会自动重启该服务。
        //START_REDELIVER_INTENT：重传Intent。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统会自动重启该服务，并将Intent的值传入。
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        unregisterReceiver(receiver);
    }
}
