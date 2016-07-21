package com.hanwen.chinesechat.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.bean.UpgradePatch;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.R;

import java.io.File;

public class ActivityAbout extends Activity implements View.OnClickListener {
    private static final String TAG = "ActivityAbout";
    private AlertDialog isNowSetupDialog;
    private AlertDialog isDownloadDialog;
    private PackageInfo packageInfo;//客户端版本信息
    private UpgradePatch upgradePatch;//服务端版本信息
    private TextView tv_tips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initView();
    }

    private void initView() {
        ImageView iv_icon = (ImageView) findViewById(R.id.iv_icon);
        iv_icon.setImageResource(ChineseChat.isStudent() ? R.drawable.ic_launcher_student : R.drawable.ic_launcher_teacher);

        findViewById(R.id.rl_version).setOnClickListener(this);
        findViewById(R.id.rl_usehelp).setOnClickListener(this);
        findViewById(R.id.rl_useterm).setOnClickListener(this);
        findViewById(R.id.iv_home).setOnClickListener(this);
        tv_tips = (TextView) findViewById(R.id.tv_tips);

        try {
            TextView versionName = (TextView) findViewById(R.id.tv_versionName);
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
            versionName.setText(packageInfo.versionName);
        } catch (Exception ex) {
            Log.i(TAG, "initView: " + ex.getMessage());
        }

        upgradePatch = new UpgradePatch();
        SharedPreferences sharedPreferences = getSharedPreferences("version", MODE_PRIVATE);
        upgradePatch.VersionName = sharedPreferences.getString("VersionName", packageInfo.versionName);
        upgradePatch.UpgradeInfo = sharedPreferences.getString("UpgradeInfo", "");
        upgradePatch.PackageSize = sharedPreferences.getLong("PackageSize", 0);
        upgradePatch.PackagePath = sharedPreferences.getString("PackagePath", "");
        if (TextUtils.equals(packageInfo.versionName, upgradePatch.VersionName)) {
            tv_tips.setText(R.string.ActivityAbout_already_latest);
        } else {
            tv_tips.setText(R.string.ActivityAbout_have_new_version);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_home:
                this.finish();
                break;
            case R.id.rl_version: {
                if (TextUtils.equals(packageInfo.versionName, upgradePatch.VersionName) || TextUtils.isEmpty(upgradePatch.PackagePath)) {
                    CommonUtil.toast(R.string.ActivityAbout_already_up_to_date);
                } else {
                    if (isDownloadDialog == null) {
                        builderDownloadDialog(upgradePatch);
                    }
                    isDownloadDialog.show();
                }
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

    private void downloadUpdatePackage() {
        boolean autoResume = false; // 如果目标文件存在，接着未完成的部分继续下载。服务器不支持RANGE时将从新下载。
        boolean autoRename = false; // 如果从请求返回信息中获取到文件名，下载完成后自动重命名。
        String path = "Download/" + packageInfo.packageName + (ChineseChat.isStudent() ? "_student_" : "_teacher_") + upgradePatch.VersionName + ".apk";
        final File installPack = new File(Environment.getExternalStorageDirectory(), path);
        new HttpUtils().download(NetworkUtil.getFullPath(upgradePatch.PackagePath), installPack.getAbsolutePath(), autoResume, autoRename, new RequestCallBack<File>() {
            private NotificationManager notificationManager;
            private NotificationCompat.Builder builder;

            @Override
            public void onFailure(HttpException error, String msg) {
                notificationManager.cancel(0);
                CommonUtil.toast(R.string.ActivityAbout_download_failure);
                Log.i(TAG, "onFailure: " + error + " msg:" + msg);
            }

            @Override
            public void onSuccess(ResponseInfo<File> responseInfo) {
                // 下载完毕,询问是否同在安装
                if (isNowSetupDialog == null) {
                    builderNowSetupDialog(responseInfo);
                }
                isNowSetupDialog.show();
                notificationManager.cancel(0);
                Log.i(TAG, "onSuccess: " + responseInfo.result.getAbsolutePath());
            }

            public void onLoading(long total, long current, boolean isUploading) {
                builder.setProgress((int) total, (int) current, false);
                notificationManager.notify(0, builder.build());
                Log.i(TAG, "onLoading: " + current + "/" + total);
            }

            public void onStart() {
                notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                builder = new NotificationCompat.Builder(ChineseChat.getContext());
                builder.setSmallIcon(R.drawable.ic_launcher_student);
                builder.setContentTitle("ChineseChat");
                builder.setContentText(ChineseChat.getContext().getString(R.string.ActivityAbout_app_update));
                builder.setProgress(100, 0, false);
                notificationManager.notify(0, builder.build());

                CommonUtil.toast(R.string.ActivityAbout_download_start);
            }
        });
    }

    private void builderDownloadDialog(final UpgradePatch upgradePatch) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityAbout.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setTitle(R.string.ActivityAbout_dialog_title);
        builder.setMessage(upgradePatch.UpgradeInfo);
        builder.setPositiveButton(R.string.ActivityAbout_dialog_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadUpdatePackage();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityAbout.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setTitle(R.string.ActivityAbout_dialog_title);
        builder.setMessage(R.string.ActivityMain_has_download_message);
        builder.setPositiveButton(R.string.ActivityMain_positive_text, new DialogInterface.OnClickListener() {

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
        builder.setNegativeButton(R.string.ActivityMain_negative_text, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        isNowSetupDialog = builder.create();
    }
}
