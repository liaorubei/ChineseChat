package com.hanwen.chinesechat.service;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.NotificationManager;
import android.app.Service;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.bean.UpgradePatch;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.io.File;

/**
 * 自动更新服务
 *
 * @author liaorubei
 */
public class AutoUpdateService extends Service {
    private static final String TAG = "AutoUpdateService";
    private File installPack;
    private AlertDialog isDownloadDialog;// 是否现在下载对话框
    private AlertDialog isNowSetupDialog;// 是否现在安装对话框
    private UpgradePatch upgradePatch;

    private void builderDownloadDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AutoUpdateService.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setTitle(R.string.ActivityMain_upgrade_tips);
        builder.setMessage(getString(R.string.ActivityMain_new_versions_message, upgradePatch.UpgradeInfo));
        builder.setPositiveButton(R.string.ActivityMain_positive_text, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 2.检查是否已经下载了安装包
                if (false)// installPack != null && installPack.exists())
                {
                    // 如果已经下载了最新安装包.询问是否现在安装
                    if (isNowSetupDialog == null) {
                        builderNowSetupDialog();
                    }
                    isNowSetupDialog.show();
                } else {
                    // 如果还没有下载最新安装包,则开始下载最新安装包
                    boolean autoResume = true; // 如果目标文件存在，接着未完成的部分继续下载。服务器不支持RANGE时将从新下载。
                    boolean autoRename = false; // 如果从请求返回信息中获取到文件名，下载完成后自动重命名。
                    new HttpUtils().download(NetworkUtil.getFullPath(upgradePatch.PackagePath), installPack.getAbsolutePath(), autoResume, autoRename, new RequestCallBack<File>() {
                        private NotificationManager notificationManager;
                        private NotificationCompat.Builder builder;

                        @Override
                        public void onFailure(HttpException error, String msg) {
                            if (installPack.exists()) {
                                installPack.delete();// 如果更新包下载失败则删除下载不完全的包
                            }
                            notificationManager.cancel(0);
                            CommonUtil.toast(R.string.AutoUpdateService_download_failed);
                        }

                        @Override
                        public void onSuccess(ResponseInfo<File> responseInfo) {
                            // 下载完毕,询问是否同在安装
                            if (isNowSetupDialog == null) {
                                builderNowSetupDialog();
                            }
                            isNowSetupDialog.show();
                            notificationManager.cancel(0);
                        }

                        public void onLoading(long total, long current, boolean isUploading) {
                            builder.setProgress((int) total, (int) current, false);
                            notificationManager.notify(0, builder.build());
                        }

                        public void onStart() {
                            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            builder = new NotificationCompat.Builder(AutoUpdateService.this);
                            builder.setTicker("开始下载更新包...");
                            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), ChineseChat.isStudent() ? R.drawable.ic_launcher_student : R.drawable.ic_launcher_teacher));
                            builder.setSmallIcon(R.drawable.download_normal);
                            builder.setContentTitle("ChineseChat");
                            builder.setContentText(getString(R.string.AutoUpdateService_app_update));
                            builder.setProgress(100, 0, false);
                            notificationManager.notify(0, builder.build());

                            CommonUtil.toast(R.string.AutoUpdateService_download_start);
                        }
                    });
                }

            }
        }).setNegativeButton(R.string.ActivityMain_negative_text, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                isDownloadDialog.dismiss();

                //如果取消更新，退出本服务
                stopSelf();
            }
        });
        isDownloadDialog = builder.create();
        isDownloadDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);// 服务中弹出对话框
    }

    private void builderNowSetupDialog() {
        Builder builder = new AlertDialog.Builder(getApplicationContext(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setTitle(R.string.ActivityMain_upgrade_tips);
        builder.setMessage(R.string.ActivityMain_has_download_message);
        builder.setPositiveButton(R.string.ActivityMain_positive_text, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 安装新版本
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(installPack), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

        });
        builder.setNegativeButton(R.string.ActivityMain_negative_text, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                isNowSetupDialog.dismiss();
                stopSelf();
            }
        });
        isNowSetupDialog = builder.create();
        isNowSetupDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);// 服务中弹出对话框
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 1.请求网络,成功时验证目前的VersionName是否与网络上的VersionName一致
        // 2.如果不一致,则确认是否要下载最新安装包,
        // 3.如果要下载,根据PackageName与VersionName检查本地是否已经下载有安装包,如果有安装包,则直接弹出安装界面
        // 3.如果之前没有下载到最新的安装包,则下载并重命名安装包,再弹出安装界面

        // 1.请求网络---升级数据请求,取得所有的升级数据，
        HttpUtil.Parameters parameters = new HttpUtil.Parameters();
        parameters.add("versionType", (ChineseChat.isStudent() ? 0 : 1) + "");
        HttpUtil.post(NetworkUtil.checkUpdate, parameters, new RequestCallBack<String>() {
            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "版本检查失败," + msg);
                stopSelf();
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                try {
                    upgradePatch = new Gson().fromJson(responseInfo.result, UpgradePatch.class);
                    //把网上的版本信息保存起来，在关于的时候会用到，
                    SharedPreferences.Editor editor = getSharedPreferences("version", MODE_PRIVATE).edit();
                    editor.putString("VersionName", upgradePatch.VersionName);
                    editor.putString("UpgradeInfo", upgradePatch.UpgradeInfo);
                    editor.putLong("PackageSize", upgradePatch.PackageSize);
                    editor.putString("PackagePath", upgradePatch.PackagePath);
                    editor.apply();

                    PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);

                    File download = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

                    installPack = new File(Environment.getExternalStorageDirectory(), "Download/" + packageInfo.packageName + "_" + upgradePatch.VersionName + (ChineseChat.isStudent() ? "_Student" : "_Teacher") + ".apk");
                    if (!installPack.getParentFile().exists()) {
                        installPack.getParentFile().mkdirs();
                    }
                    Log.i(TAG, "版本检查成功,网络VersionName=" + upgradePatch.VersionName + ",本身versionName=" + packageInfo.versionName + ",更新地址=" + upgradePatch.PackagePath);

                    // 检查当前versionName与网络上最新的VersionName是否一致,如果不一致则进入
                    if (!packageInfo.versionName.equals(upgradePatch.VersionName)) {
                        if (isDownloadDialog == null) {
                            builderDownloadDialog();
                        }
                        isDownloadDialog.show();
                    } else {
                        //如果是最新版本，则不用更新，退出本服务
                        stopSelf();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        return Service.START_NOT_STICKY;
    }
}
