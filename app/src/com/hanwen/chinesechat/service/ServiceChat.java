package com.hanwen.chinesechat.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.AVChatStateObserver;

public class ServiceChat extends Service {

    private MyBinder binder;

    @Override
    public void onCreate() {
        super.onCreate();
        this.binder = new MyBinder();


        AVChatManager.getInstance().observeAVChatState(new AVChatStateObserver() {
            @Override
            public void onTakeSnapshotResult(String s, boolean b, String s1) {

            }

            @Override
            public void onConnectionTypeChanged(int i, int i1) {

            }

            @Override
            public void onLocalRecordEnd(String[] strings, int i) {

            }

            @Override
            public void onFirstVideoFrameAvailable(String s) {

            }

            @Override
            public void onVideoFpsReported(String s, int i) {

            }

            @Override
            public void onJoinedChannel(int i, String s, String s1) {

            }

            @Override
            public void onLeaveChannel() {

            }

            @Override
            public void onUserJoined(String s) {

            }

            @Override
            public void onUserLeave(String s, int i) {

            }

            @Override
            public void onProtocolIncompatible(int i) {

            }

            @Override
            public void onDisconnectServer() {

            }

            @Override
            public void onNetworkQuality(String s, int i) {

            }

            @Override
            public void onCallEstablished() {

            }

            @Override
            public void onDeviceEvent(String s, int i, String s1) {

            }
        }, true);
    }

    @Override
    public IBinder onBind(Intent intent) {


        return this.binder;
    }

    private class MyBinder extends Binder {
    }
}
