package com.hanwen.chinesechat;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;

import com.hanwen.chinesechat.activity.ActivitySignIn;
import com.hanwen.chinesechat.activity.ActivityTake;
import com.hanwen.chinesechat.bean.ChatData;
import com.hanwen.chinesechat.bean.User;
import com.hanwen.chinesechat.database.Database;
import com.hanwen.chinesechat.service.ServiceQueue;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.SystemUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.auth.OnlineClient;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.model.AVChatData;

import java.io.File;
import java.util.List;

public class ChineseChat extends Application {
    protected static final String TAG = "ChineseChat";
    private static Context mContext = null;
    private static Database mDatabase = null;
    public static User CurrentUser;

    private static String release = "student";

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mDatabase = new Database(this);

        // 注意：除了 NIMClient.init 接口外，其他 SDK 暴露的接口都只能在 UI 进程调用。
        // 如果 APP 包含远程 service，该 APP 的 Application 的 onCreate 会多次调用。
        // 因此，如果需要在 onCreate 中调用除 init 接口外的其他接口，应先判断当前所属进程，并只有在当前是 UI 进程时才调用。
        NIMClient.init(this, getLoginInfo(), getOptions());

        if (SystemUtil.inMainProcess(this)) {
            //region 注册来电监听
            AVChatManager.getInstance().observeIncomingCall(new Observer<AVChatData>() {
                @Override
                public void onEvent(AVChatData chatData) {
                    Log.i(TAG, "来电监听: " + chatData);
                    ChatData chat = new ChatData();
                    chat.setChatId(chatData.getChatId());
                    chat.setAccid(chatData.getAccount());
                    chat.setChatType(chatData.getChatType());
                    chat.setExtra(chatData.getExtra());
                    ActivityTake.start(getApplicationContext(), ActivityTake.CHAT_MODE_INCOMING, chat);
                }
            }, true);

            NIMClient.getService(AuthServiceObserver.class).observeOtherClients(new Observer<List<OnlineClient>>() {
                @Override
                public void onEvent(List<OnlineClient> onlineClients) {
                    Log.i(TAG, "onEvent: observeOtherClients=" + onlineClients + " " + this);
                }
            }, true);
            //endregion

            //region 云信状态监听
            NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(new Observer<StatusCode>() {
                @Override
                public void onEvent(StatusCode statusCode) {

                    if (!isStudent()) {
                        Intent service = new Intent(ChineseChat.this, ServiceQueue.class);
                        //service.addFlags(Intent.ACTION_GTALK_SERVICE_CONNECTED)
                        startService(service);
                    }

                    Log.i(TAG, "云信状态: " + statusCode);
                    switch (statusCode) {

                        case KICK_BY_OTHER_CLIENT:
                        case KICKOUT:
                            getSharedPreferences("user", MODE_PRIVATE).edit().clear().commit();
                            CurrentUser = new User();

                            //
                            // TODO: 2016-07-08  如果当前通话状态为正在进行时,挂断通话

                            ActivitySignIn.startFromKickout(getApplicationContext());
                            break;
                    }
                }
            }, true);
            //endregion

            //是学生端还是教师端
            try {
                release = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData.getString("release");
                Log.i(TAG, "release: " + release);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private SDKOptions getOptions() {
        SDKOptions options = new SDKOptions();
        options.appKey = "599551c5de7282b9a1d686ee40abf74c";
        options.sdkStorageRootPath = new File(Environment.getExternalStorageDirectory(), getPackageName() + "/nim").getAbsolutePath();
        return options;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.i(TAG, "onTerminate: ");
    }

    private LoginInfo getLoginInfo() {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        String accid = sp.getString("accid", "");
        String token = sp.getString("token", "");
        CurrentUser = new User();
        CurrentUser.Accid = accid;
        CurrentUser.Token = token;
        CurrentUser.Id = sp.getInt("id", -1);
        CurrentUser.Username = sp.getString("username", null);
        CurrentUser.PassWord = sp.getString("password", null);
        CurrentUser.Nickname = sp.getString("nickname", null);
        CurrentUser.Mobile = sp.getString("mobile", null);
        CurrentUser.Avatar = sp.getString("avatar", null);
        CurrentUser.Gender = sp.getInt("gender", -1);
        CurrentUser.Coins = sp.getInt("coins", 0);
        CurrentUser.Birth = sp.getString("birth", null);

        return TextUtils.isEmpty(accid) || TextUtils.isEmpty(token) ? null : new LoginInfo(accid, token);
    }

    public static Context getContext() {
        return mContext;
    }

    public static boolean isStudent() {
        return "student".equals(release);
    }

    public static Database getDatabase() {
        return mDatabase;
    }
}
