package com.newclass.woyaoxue;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
import com.newclass.woyaoxue.activity.MessageActivity;
import com.newclass.woyaoxue.database.Database;
import com.newclass.woyaoxue.util.ConstantsUtil;
import com.newclass.woyaoxue.util.Log;
import com.voc.woyaoxue.R;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class MyApplication extends Application {
    protected static final String TAG = "MyApplication";
    private static Context mContext = null;
    private static Database mDatabase = null;
    private Observer<StatusCode> observerOnlineStatus = new Observer<StatusCode>() {
        private static final long serialVersionUID = 1L;

        @Override
        public void onEvent(StatusCode code) {
            Log.i(TAG, "StatusCode=" + code + " 实例:" + this);
        }
    };
    private static String release = "student";

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mDatabase = new Database(this);
        SDKOptions options = getOptions();
        LoginInfo loginInfo = getLoginInfo();
        NIMClient.init(this, loginInfo, options);

        try {
            release = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData.getString("release");
            Log.i(TAG, "release: " + release);
        } catch (Exception e) {
            Log.i(TAG, "onCreate: " + e.getMessage());
        }
    }

    private SDKOptions getOptions() {
        SDKOptions options = new SDKOptions();
        options.appKey = "599551c5de7282b9a1d686ee40abf74c";

        // 如果将新消息通知提醒托管给 SDK 完成，需要添加以下配置。否则无需设置。
        StatusBarNotificationConfig config = new StatusBarNotificationConfig();
        config.notificationEntrance = MessageActivity.class; // 点击通知栏跳转到该Activity
        config.notificationSmallIconId = R.drawable.ic_launcher;
        options.statusBarNotificationConfig = config;

        // 配置保存图片，文件，log 等数据的目录
        // 如果 options 中没有设置这个值，SDK 会使用下面代码示例中的位置作为 SDK 的数据目录。
        // 该目录目前包含 log, file, image, audio, video, thumb 这6个目录。
        // 如果第三方 APP 需要缓存清理功能， 清理这个目录下面个子目录的内容即可。
        String sdkPath = Environment.getExternalStorageDirectory() + "/" + getPackageName() + "/nim";
        options.sdkStorageRootPath = sdkPath;

        // 配置是否需要预下载附件缩略图，默认为 true
        options.preloadAttach = true;

        // 配置附件缩略图的尺寸大小，该值一般应根据屏幕尺寸来确定， 默认值为 Screen.width / 2
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        options.thumbnailSize = outMetrics.widthPixels / 2;

        // 用户资料提供者, 目前主要用于提供用户资料，用于新消息通知栏中显示消息来源的头像和昵称
        options.userInfoProvider = new UserInfoProvider() {
            @Override
            public UserInfo getUserInfo(String account) {
                return null;
            }

            @Override
            public int getDefaultIconResId() {
                return R.drawable.ic_launcher;
            }

            @Override
            public Bitmap getTeamIcon(String tid) {
                return null;
            }

            @Override
            public Bitmap getAvatarForMessageNotifier(String account) {
                return null;
            }

            @Override
            public String getDisplayNameForMessageNotifier(String account, String sessionId, SessionTypeEnum sessionType) {
                return null;
            }
        };
        return options;
    }

    private LoginInfo getLoginInfo() {
        LoginInfo info = null;
        boolean is_release = false;
        try {
            is_release = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData.getBoolean("IS_RELEASE", false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (is_release) {

            SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
            String accid = sp.getString("accid", "");
            String token = sp.getString("token", "");

            if (TextUtils.isEmpty(accid) || TextUtils.isEmpty(token)) {
                return null;
            } else {
                info = new LoginInfo(accid, token);
            }
        }
        Log.i(TAG, "getLoginInfo: " + info);
        return info;
    }

    public static Context getContext() {
        return mContext;
    }

    public static void setContext(Context context) {
        mContext = context;
    }


    public static boolean isStudent() {
        return "student".equals(release);
    }

    public static Database getDatabase() {
        return mDatabase;
    }
}
