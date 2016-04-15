package com.newclass.woyaoxue.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.newclass.woyaoxue.ChineseChat;
import com.newclass.woyaoxue.bean.UpgradePatch;
import com.newclass.woyaoxue.service.AutoUpdateService;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import java.io.File;

public class ActivityAbout extends Activity implements View.OnClickListener {
    private static final String TAG = "ActivityAbout";
    private AlertDialog isNowSetupDialog;
    private AlertDialog isDownloadDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initView();
    }

    private void initView() {
        findViewById(R.id.rl_version).setOnClickListener(this);
        findViewById(R.id.rl_usehelp).setOnClickListener(this);
        findViewById(R.id.rl_useterm).setOnClickListener(this);
        findViewById(R.id.iv_home).setOnClickListener(this);
        try {
            TextView versionName = (TextView) findViewById(R.id.tv_versionName);
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
            versionName.setText(packageInfo.versionName);
        } catch (Exception ex) {
            Log.i(TAG, "initView: " + ex.getMessage());
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_home:
                this.finish();
                break;
            case R.id.rl_version: {
                HttpUtil.Parameters parameters = new HttpUtil.Parameters();
                parameters.add("versionType", (ChineseChat.isStudent() ? 0 : 1));
                HttpUtil.post(NetworkUtil.checkUpdate, parameters, new RequestCallBack<String>() {
                    @Override
                    public void onFailure(HttpException error, String msg) {
                        Log.i(TAG, "查询更新失败: " + msg);
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        Log.i(TAG, "onSuccess: " + responseInfo.result);
                        try {
                            UpgradePatch upgradePatch = new Gson().fromJson(responseInfo.result, UpgradePatch.class);
                            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
                            File installPack = new File(Environment.getExternalStorageDirectory(), "Download/" + packageInfo.packageName + "_" + upgradePatch.VersionName + (ChineseChat.isStudent() ? "_Student" : "_Teacher") + ".apk");
                            boolean exists = installPack.getParentFile().exists() || installPack.getParentFile().mkdirs();

                            // 检查当前versionName与网络上最新的VersionName是否一致,如果不一致则进入
                            if (TextUtils.equals(packageInfo.versionName, upgradePatch.VersionName)) {
                                CommonUtil.toast(R.string.ActivityAbout_already_up_to_date);
                            } else {
                                // 询问是否要下载最新版本
                                if (exists) {
                                    if (isDownloadDialog == null) {
                                        builderDownloadDialog(upgradePatch, installPack);
                                    }
                                    isDownloadDialog.show();
                                } else {
                                    CommonUtil.toast(R.string.ActivityAbout_check_sdcard);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
            break;
            case R.id.rl_usehelp: {
                Intent intent = new Intent(getApplicationContext(), ActivityUsehelp.class);
                startActivity(intent);
            }
            break;
            case R.id.rl_useterm: {
                Intent intent = new Intent(getApplicationContext(), ActivityUseterm.class);
                startActivity(intent);
            }
            break;
        }
    }

    private void builderDownloadDialog(final UpgradePatch upgradePatch, final File installPack) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setTitle(R.string.ActivityAbout_title);
        builder.setMessage(upgradePatch.UpgradeInfo);
        builder.setPositiveButton(R.string.ActivityAbout_dialog_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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
                        CommonUtil.toast(R.string.ActivityAbout_download_failure);
                    }

                    @Override
                    public void onSuccess(ResponseInfo<File> responseInfo) {
                        // 下载完毕,询问是否同在安装
                        if (isNowSetupDialog == null) {
                            builderNowSetupDialog(responseInfo);
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
                        builder = new NotificationCompat.Builder(ChineseChat.getContext());
                        builder.setSmallIcon(R.drawable.ic_launcher);
                        builder.setContentTitle("ChineseChat");
                        builder.setContentText(ChineseChat.getContext().getString(R.string.ActivityAbout_app_update));
                        builder.setProgress(100, 0, false);
                        notificationManager.notify(0, builder.build());

                        CommonUtil.toast(R.string.ActivityAbout_download_start);
                    }
                });

            }
        });
        builder.setNegativeButton(R.string.ActivityAbout_dialog_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        isDownloadDialog = builder.create();
    }

    private void builderNowSetupDialog(final ResponseInfo<File> responseInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setTitle(R.string.upgrade_tips);
        builder.setMessage(R.string.has_download_message);
        builder.setPositiveButton(R.string.positive_text, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 安装新版本
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(responseInfo.result), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                dialog.dismiss();
            }

        });
        builder.setNegativeButton(R.string.negative_text, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        isNowSetupDialog = builder.create();
    }
}
