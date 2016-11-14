package com.hanwen.chinesechat.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class ServiceChat extends Service {

    private MyBinder binder;

    @Override
    public void onCreate() {
        super.onCreate();
        this.binder = new MyBinder();
    }

    @Override
    public IBinder onBind(Intent intent) {


        return this.binder;
    }

    private class MyBinder extends Binder {
    }
}
