package com.hanwen.chinesechat.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;

import com.hanwen.chinesechat.activity.ActivityChat;
import com.hanwen.chinesechat.util.Log;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.AVChatStateObserver;
import com.netease.nimlib.sdk.avchat.model.AVChatAudioFrame;
import com.netease.nimlib.sdk.avchat.model.AVChatCalleeAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatOnlineAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoFrame;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.MessageReceipt;

import java.util.List;
import java.util.Map;

/*
实时语音通话实例，
 */
public class ServiceChat extends Service {

    private static final String TAG = "ServiceChat";
    public static final String KEY_CHAT_DATA = "KEY_CHAT_DATA";
    private AVChatData chatData;
    private int chatMode;
    private AVChatCallback<Void> callback = new AVChatCallback<Void>() {
        @Override
        public void onSuccess(Void aVoid) {

        }

        @Override
        public void onFailed(int code) {

        }

        @Override
        public void onException(Throwable exception) {

        }
    };

    private boolean callEstablished = false;
    private AVChatStateObserver observerChatState = new AVChatStateObserver() {
        @Override
        public void onTakeSnapshotResult(String account, boolean success, String file) {

        }

        @Override
        public void onConnectionTypeChanged(int netType) {

        }

        @Override
        public void onLocalRecordEnd(String[] files, int event) {

        }

        @Override
        public void onFirstVideoFrameAvailable(String account) {

        }

        @Override
        public void onVideoFpsReported(String account, int fps) {

        }

        @Override
        public void onJoinedChannel(int code, String audioFile, String videoFile) {

        }

        @Override
        public void onLeaveChannel() {

        }

        @Override
        public void onUserJoined(String account) {

        }

        @Override
        public void onUserLeave(String account, int event) {

        }

        @Override
        public void onProtocolIncompatible(int status) {

        }

        @Override
        public void onDisconnectServer() {

        }

        @Override
        public void onNetworkQuality(String user, int value) {

        }

        @Override
        public void onCallEstablished() {
            Log.i(TAG, "onCallEstablished: 通话确立");
            callEstablished = true;
            establishedBaseTime = SystemClock.elapsedRealtime();
        }

        @Override
        public void onDeviceEvent(int code, String desc) {
            Log.i(TAG, "onDeviceEvent: " + code + "," + desc);
        }

        @Override
        public void onFirstVideoFrameRendered(String user) {

        }

        @Override
        public void onVideoFrameResolutionChanged(String user, int width, int height, int rotate) {

        }

        @Override
        public int onVideoFrameFilter(AVChatVideoFrame frame) {
            return 0;
        }

        @Override
        public int onAudioFrameFilter(AVChatAudioFrame frame) {
            return 0;
        }

        @Override
        public void onAudioOutputDeviceChanged(int device) {

        }

        @Override
        public void onReportSpeaker(Map<String, Integer> speakers, int mixedEnergy) {

        }

        @Override
        public void onStartLiveResult(int code) {

        }

        @Override
        public void onStopLiveResult(int code) {

        }
    };
    private Observer<AVChatCalleeAckEvent> observerCalleeAckEvent = new Observer<AVChatCalleeAckEvent>() {
        @Override
        public void onEvent(AVChatCalleeAckEvent avChatCalleeAckEvent) {
            Log.i(TAG, "onEvent: " + avChatCalleeAckEvent);
        }
    };
    private Observer<AVChatCommonEvent> observerHangUp = new Observer<AVChatCommonEvent>() {
        @Override
        public void onEvent(AVChatCommonEvent avChatCommonEvent) {
            Log.i(TAG, "onEvent: " + avChatCommonEvent);
        }
    };
    private Observer<AVChatOnlineAckEvent> observerOnlineAck = new Observer<AVChatOnlineAckEvent>() {
        @Override
        public void onEvent(AVChatOnlineAckEvent avChatOnlineAckEvent) {
            Log.i(TAG, "onEvent: " + avChatOnlineAckEvent);
        }
    };

    private Observer<List<MessageReceipt>> ObserverBaseMessage = new Observer<List<MessageReceipt>>() {
        @Override
        public void onEvent(List<MessageReceipt> messageReceipts) {

        }
    };
    private Observer<CustomNotification> ObserverCustomNotify = new Observer<CustomNotification>() {
        @Override
        public void onEvent(CustomNotification customNotification) {

        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
        registerNimObserver(true);
    }

    private void registerNimObserver(boolean register) {
        //实时语音监听
        AVChatManager.getInstance().observeAVChatState(observerChatState, register);

        AVChatManager.getInstance().observeCalleeAckNotification(observerCalleeAckEvent, register);
        AVChatManager.getInstance().observeHangUpNotification(observerHangUp, register);
        AVChatManager.getInstance().observeOnlineAckNotification(observerOnlineAck, register);

        //消息监听
        NIMClient.getService(MsgServiceObserve.class).observeMessageReceipt(ObserverBaseMessage, register);
        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(ObserverCustomNotify, register);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind: ");
        return new MyBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        chatData = (AVChatData) intent.getSerializableExtra(KEY_CHAT_DATA);
        chatMode = intent.getIntExtra(ActivityChat.KEY_CHAT_MODE, -1);
        Log.i(TAG, "onStartCommand: " + this);
        Log.i(TAG, "onStartCommand: " + chatData + ",Extra=" + chatData.getExtra());

        //  NIMClient.getService(MsgService.class).



/*        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setTicker("实时语音通话正在进行，点击开打界面");
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_student));
        builder.setSmallIcon(R.drawable.ic_launcher_teacher);
        builder.setContentTitle("ContentTitle");
        builder.setContentText("ContentText");
        builder.setSubText("SubText");
        builder.setOngoing(true);//常驻
        builder.setAutoCancel(false);//无法取消
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE | NotificationCompat.FLAG_NO_CLEAR;
        startForeground(12305, notification);*/
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        registerNimObserver(false);
    }


    private long establishedBaseTime;
    private ActivityChat activityChat;

    public class MyBinder extends Binder {
        public boolean isCallEstablished() {
            return ServiceChat.this.callEstablished;
        }

        public long getEstablishedBaseTime() {
            return establishedBaseTime;
        }
    }
}
