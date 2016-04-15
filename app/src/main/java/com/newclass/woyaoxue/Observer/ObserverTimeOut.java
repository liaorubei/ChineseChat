package com.newclass.woyaoxue.Observer;

import android.app.Activity;

import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.avchat.constant.AVChatTimeOutEvent;
import com.newclass.woyaoxue.util.Log;

/**
 * Created by 儒北 on 2016-04-14.
 */
public class ObserverTimeOut implements Observer<AVChatTimeOutEvent> {
    private static final String TAG = "ObserverTimeOut";
    private final Activity mActivity;

    public ObserverTimeOut(Activity activity) {
        this.mActivity = activity;
    }

    @Override
    public void onEvent(AVChatTimeOutEvent avChatTimeOutEvent) {
        Log.i(TAG, "onEvent: 通话超时");
        this.mActivity.finish();
    }
}
