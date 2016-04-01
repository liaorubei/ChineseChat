package com.newclass.woyaoxue.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import android.text.format.Formatter;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.FileUtil;
import com.voc.woyaoxue.R;

import java.io.File;

public class ActivitySetting extends Activity implements View.OnClickListener {
    private static final String TAG = "SettingActivity";
    private RelativeLayout rl_feedback, rl_login;
    private TextView tv_login;
    private ImageView iv_home;
    private TextView tv_cache;
    private Dialog dialogWipeData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
        initData();
    }

    private void initData() {

        //清除缓存菜单数据
        long length = FileUtil.fileLength(getFilesDir()) + FileUtil.fileLength(getCacheDir()) + FileUtil.fileLength(new File(getFilesDir().getParentFile(), "databases"));
        tv_cache.setText(Formatter.formatFileSize(getApplicationContext(), length));

    }

    private void initView() {
        //标题栏
        iv_home = (ImageView) findViewById(R.id.iv_home);
        findViewById(R.id.rl_feedback).setOnClickListener(this);
        findViewById(R.id.rl_wipedata).setOnClickListener(this);
        findViewById(R.id.rl_aboutapp).setOnClickListener(this);


        tv_cache = (TextView) findViewById(R.id.tv_cache);


        rl_login = (RelativeLayout) findViewById(R.id.rl_login);
        tv_login = (TextView) findViewById(R.id.tv_login);

        tv_login.setText(NIMClient.getStatus() == StatusCode.LOGINED ? "登出" : "登录");

        iv_home.setOnClickListener(this);
        rl_login.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.iv_home:
                this.finish();
                break;
            case R.id.rl_feedback: {
                Intent intent = new Intent(getApplicationContext(), ActivityFeedback.class);
                startActivity(intent);
            }
            break;
            case R.id.rl_wipedata: {
                if (dialogWipeData == null) {
                    dialogWipeData = new Dialog(this);
                    dialogWipeData.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialogWipeData.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    dialogWipeData.setContentView(R.layout.dialog_delete);
                    dialogWipeData.findViewById(R.id.bt_positive).setOnClickListener(this);
                    dialogWipeData.findViewById(R.id.bt_negative).setOnClickListener(this);
                    dialogWipeData.setCancelable(true);
                    dialogWipeData.setCanceledOnTouchOutside(true);
                }
                dialogWipeData.show();
            }
            break;
            case R.id.bt_positive: {//清除缓存——确定
                FileUtil.fileDelete(getCacheDir());
                FileUtil.fileDelete(getFilesDir());
                FileUtil.fileDelete(new File(getFilesDir().getParentFile(), "databases"));

                //主要是清除三个文件夹的内容，cache,files,databases
                CommonUtil.toast("清除完毕");
                tv_cache.setText("0.00B");
                dialogWipeData.dismiss();
            }
            break;
            case R.id.bt_negative: {//清除缓存——取消
                dialogWipeData.dismiss();
            }
            break;
            case R.id.rl_aboutapp: {
                Intent intent = new Intent(getApplicationContext(), ActivityAbout.class);
                startActivity(intent);
            }
            break;
            case R.id.rl_login:
                if (NIMClient.getStatus() == StatusCode.LOGINED) {
                    NIMClient.getService(AuthService.class).logout();
                    getSharedPreferences("user", MODE_PRIVATE).edit().clear().commit();
                    CommonUtil.toast("登出成功");
                    tv_login.setText(NIMClient.getStatus() == StatusCode.LOGINED ? "登出" : "登录");
                } else {
                    startActivity(new Intent(this, ActivitySignIn.class));
                }
                break;


        }
    }
}
