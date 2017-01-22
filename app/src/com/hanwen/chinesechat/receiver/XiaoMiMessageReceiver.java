package com.hanwen.chinesechat.receiver;

import android.content.Context;

import com.hanwen.chinesechat.util.Log;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

/**
 * Created by ChineseChat on 2016/12/20.
 * 小米推送服务回调方法
 */
public class XiaoMiMessageReceiver extends PushMessageReceiver {
    private static final String TAG = "XiaoMiMessageReceiver";

    //onCommandResult用来接收客户端向服务器发送命令消息后返回的响应
    @Override
    public void onCommandResult(Context context, MiPushCommandMessage miPushCommandMessage) {
        super.onCommandResult(context, miPushCommandMessage);
        Log.i(TAG, "onCommandResult: ");
    }

    //onNotificationMessageArrived用来接收服务器发来的通知栏消息（消息到达客户端时触发，并且可以接收应用在前台时不弹出通知的通知消息）
    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage miPushMessage) {
        super.onNotificationMessageArrived(context, miPushMessage);
        Log.i(TAG, "onNotificationMessageArrived: ");
    }

    //onNotificationMessageClicked用来接收服务器发来的通知栏消息（用户点击通知栏时触发）
    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage miPushMessage) {
        super.onNotificationMessageClicked(context, miPushMessage);
        Log.i(TAG, "onNotificationMessageClicked: ");
    }

    //onReceivePassThroughMessage用来接收服务器发送的透传消息，
    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage miPushMessage) {
        super.onReceivePassThroughMessage(context, miPushMessage);
        Log.i(TAG, "onReceivePassThroughMessage: ");
    }

    //onReceiveRegisterResult用来接受客户端向服务器发送注册命令消息后返回的响应。
    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage miPushCommandMessage) {
        super.onReceiveRegisterResult(context, miPushCommandMessage);
        Log.i(TAG, "onReceiveRegisterResult: ");
    }
}
