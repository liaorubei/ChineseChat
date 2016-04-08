package com.newclass.woyaoxue.service;

import java.io.File;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.NotificationManager;
import android.app.Service;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.newclass.woyaoxue.ChineseChat;
import com.newclass.woyaoxue.bean.UpgradePatch;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

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
        Builder builder = new AlertDialog.Builder(getApplicationContext(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setTitle(R.string.upgrade_tips);
        String string = getResources().getString(R.string.new_versions_message);
        string += "\r\n" + upgradePatch.UpgradeInfo;
        builder.setMessage(string);
        builder.setPositiveButton(R.string.positive_text, new OnClickListener() {

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
                            builder.setContentText("下载失败");
                            notificationManager.cancel(0);

                            CommonUtil.toast("下载失败" + msg);
                        }

                        @Override
                        public void onSuccess(ResponseInfo<File> responseInfo) {
                            // 下载完毕,询问是否同在安装
                            if (isNowSetupDialog == null) {
                                builderNowSetupDialog();
                            }
                            isNowSetupDialog.show();
                            notificationManager.cancel(0);
                            ;
                        }

                        public void onLoading(long total, long current, boolean isUploading) {
                            builder.setProgress((int) total, (int) current, false);
                            notificationManager.notify(0, builder.build());
                        }

                        public void onStart() {
                            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            builder = new NotificationCompat.Builder(AutoUpdateService.this);
                            builder.setSmallIcon(R.drawable.ic_launcher);
                            builder.setContentTitle("ChineseChat");
                            builder.setContentText("应用更新");
                            builder.setProgress(100, 0, false);
                            notificationManager.notify(0, builder.build());

                            CommonUtil.toast("开始下载");
                        }
                    });
                }

            }
        });
        builder.setNegativeButton(R.string.negative_text, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                isDownloadDialog.dismiss();
            }
        });
        isDownloadDialog = builder.create();
        isDownloadDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);// 服务中弹出对话框
    }

    private void builderNowSetupDialog() {
        Builder builder = new AlertDialog.Builder(getApplicationContext(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setTitle(R.string.upgrade_tips);
        builder.setMessage(R.string.has_download_message);
        builder.setPositiveButton(R.string.positive_text, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 安装新版本
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(installPack), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

        });
        builder.setNegativeButton(R.string.negative_text, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                isNowSetupDialog.dismiss();
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


        // 1.请求网络---升级数据请求,建议放到spash界面
        HttpUtil.Parameters parameters = new HttpUtil.Parameters();
        parameters.add("versionType", (ChineseChat.isStudent()? 0 : 1) + "");
        HttpUtil.post(NetworkUtil.checkUpdate, parameters, new RequestCallBack<String>() {

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: " + "查询更新失败" + msg);
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                try {
                    upgradePatch = new Gson().fromJson(responseInfo.result, UpgradePatch.class);
                    PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
                    installPack = new File(Environment.getExternalStorageDirectory(), "Download/" + packageInfo.packageName + "_" + upgradePatch.VersionName + ".apk");
                    if (!installPack.getParentFile().exists()) {
                        Log.i("logi", "创建更新下载目录");
                        installPack.getParentFile().mkdirs();
                    }

                    // 检查当前versionName与网络上最新的VersionName是否一致,如果不一致则进入
                    if (!packageInfo.versionName.equals(upgradePatch.VersionName)) {
                        // 询问是否要下载最新版本
                        if (isDownloadDialog == null) {
                            builderDownloadDialog();
                        }
                        isDownloadDialog.show();
                    } else {
                        Log.i("logi", "最新版本 RequestUrl:" + this.getRequestUrl());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        return super.onStartCommand(intent, flags, startId);
    }
}
