package com.hanwen.chinesechat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.hanwen.chinesechat.util.Log;

public class MyReceiver extends BroadcastReceiver {
    private static final String TAG = "MyReceiver";

    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      //  Log.i(TAG, "onReceive: " + intent.getAction() + " " + intent.getStringExtra(TelephonyManager.EXTRA_STATE));
    }
}
