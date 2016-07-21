package com.hanwen.chinesechat.Observer;

import android.app.Activity;
import android.util.Log;

import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;

/**
 * Created by 儒北 on 2016-04-14.
 */
public class ObserverHangup implements Observer<AVChatCommonEvent> {
    private static final String TAG = "ObserverHangup";
    private final Activity mActivity;

    public ObserverHangup(Activity activity) {
        this.mActivity = activity;
    }

    @Override
    public void onEvent(AVChatCommonEvent avChatCommonEvent) {
        Log.i(TAG, "onEvent: 对方挂断 ChatId=" + avChatCommonEvent.getChatId() + " Account=" + avChatCommonEvent.getAccount() + " ChatType=" + avChatCommonEvent.getChatType() + " Event=" + avChatCommonEvent.getEvent());
        this.mActivity.finish();
    }
}
