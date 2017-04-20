package com.hanwen.chinesechat;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.hanwen.chinesechat.activity.ActivityChat;
import com.hanwen.chinesechat.activity.ActivitySignIn;
import com.hanwen.chinesechat.bean.User;
import com.hanwen.chinesechat.database.Database;
import com.hanwen.chinesechat.service.ServiceChat;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.SoundPlayer;
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
import com.netease.nis.bugrpt.CrashHandler;

import java.io.File;
import java.util.List;

public class ChineseChat extends Application {
    protected static final String TAG = "ChineseChat";
    private static final String MI_APP_ID = "2882303761517534747";
    private static final String MI_APP_KEY = "5461753418747";
    private static Context mContext = null;
    private static Database mDatabase = null;
    public static User CurrentUser;

    private static String release = "student";

    @Override
    public void onCreate() {
        super.onCreate();

/*        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);*/

        // 注意：除了 NIMClient.init 接口外，其他 SDK 暴露的接口都只能在 UI 进程调用。
        // 如果 APP 包含远程 service，该 APP 的 Application 的 onCreate 会多次调用。
        // 因此，如果需要在 onCreate 中调用除 init 接口外的其他接口，应先判断当前所属进程，并只有在当前是 UI 进程时才调用。
        NIMClient.init(this, getLoginInfo(), getOptions());

       // Log.i(TAG, "onCreate: " + new Gson().toJson(new DeviceUtil()));


        //主进程注册各种服务
        if (SystemUtil.inMainProcess(this)) {
            mContext = this;
            mDatabase = new Database(this);

            //网易云捕
            CrashHandler.init(getApplicationContext());

            //region 注册来电监听
            AVChatManager.getInstance().observeIncomingCall(new Observer<AVChatData>() {
                @Override
                public void onEvent(AVChatData chatData) {
                    Log.i(TAG, "来电监听: " + chatData + " ChatId=" + chatData.getChatId() + " Extra=" + chatData.getExtra());

                    SoundPlayer.instance(getApplicationContext()).play(SoundPlayer.RingerTypeEnum.RING);

                    //启动界面
                    Intent activity = new Intent(getApplicationContext(), ActivityChat.class);
                    activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.putExtra(ActivityChat.KEY_CHAT_DATA, chatData);
                    activity.putExtra(ActivityChat.KEY_CHAT_MODE, ActivityChat.CHAT_MODE_INCOMING);
                    startActivity(activity);

                    //启动服务
                    Intent service = new Intent(getApplicationContext(), ServiceChat.class);
                    service.putExtra(ActivityChat.KEY_CHAT_DATA, chatData);
                    service.putExtra(ActivityChat.KEY_CHAT_MODE, ActivityChat.CHAT_MODE_INCOMING);
                    startService(service);
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
                    Log.i(TAG, "云信状态: " + statusCode);
                    switch (statusCode) {

                        case KICK_BY_OTHER_CLIENT:
                        case KICKOUT:
                            getSharedPreferences("user", MODE_PRIVATE).edit().clear().commit();
                            CurrentUser = new User();

                            //2016-07-08  如果当前通话状态为正在进行时,挂断通话
                            ActivitySignIn.startFromKickout(getApplicationContext());
                            break;
                    }
                }
            }, true);
            //endregion

            //是学生端还是教师端
            try {
                release = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData.getString("release");
            } catch (Exception e) {
                e.printStackTrace();
            }

            //注册小米推送服务
            //开发者应确保自身业务中推送的流程和逻辑完整，虽然已经调用了NIMPushClient.registerMiPush()，仍然要在合适的时机调用 MiPushClient.registerPush() 注册推送。
            // 由于云信也会在小米设备上调用此方法，因此可能存在 MiPushMessageReceiver中 onReceiveRegisterResult 多次回调的情形。
            // 此外，小米 SDK 在 5s内重复调用 MiPushClient.registerPush() 注册推送会被过滤，因此，请开发者不要在应用生命周期中反复调用注册方法。
            //MiPushClient.registerPush(this, MI_APP_ID, MI_APP_KEY);
            //NIMPushClient.registerMiPush(this, "xiaomicertificate", MI_APP_ID, MI_APP_KEY);
        }

    }

    private SDKOptions getOptions() {
        SDKOptions options = new SDKOptions();
        options.appKey = "599551c5de7282b9a1d686ee40abf74c";
        options.sdkStorageRootPath = new File(getExternalCacheDir(), "NimClient").getAbsolutePath();//new File(Environment.getExternalStorageDirectory(), getPackageName() + "/nim").getAbsolutePath();
        //后台自动下载附件：如果是语音消息，直接下载文件，如果是图片或视频消息，下载缩略图文件。
        options.preloadAttach = true;
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

    /**
     * 上下文全局引用
     *
     * @return
     */
    public static Context getContext() {
        return mContext;
    }

    public static boolean isStudent() {
        return "student".equals(release);
    }

    /**
     * 数据库全局引用
     *
     * @return
     */
    public static Database database() {
        return mDatabase;
    }

    public static Database database(Context context) {
        if (mDatabase == null) {
            synchronized (ChineseChat.class) {
                if (mDatabase == null) {
                    mDatabase = new Database(context);
                }
            }
        }
        return mDatabase;
    }
}
