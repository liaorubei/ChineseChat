package com.newclass.woyaoxue.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
    private TextView tv_login, tv_cache;
    private Dialog dialogWipeData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
        initData();
    }

    private void initData() {
        //清除缓存菜单数据,只显示两个文件的大小,一个是自己的下载文件夹,一个是数据库文件夹
        long length = FileUtil.fileLength(new File(getFilesDir(), "File")) + FileUtil.fileLength(new File(getFilesDir().getParentFile(), "databases"));
        tv_cache.setText(Formatter.formatFileSize(getApplicationContext(), length));

        //登录菜单的文本显示
        tv_login.setText(NIMClient.getStatus() == StatusCode.LOGINED ? getString(R.string.ActivitySetting_登出) : getString(R.string.ActivitySetting_登录));
    }

    private void initView() {
        findViewById(R.id.iv_home).setOnClickListener(this);

        View rl_security = findViewById(R.id.rl_security);
        rl_security.setOnClickListener(this);

        findViewById(R.id.rl_feedback).setOnClickListener(this);
        findViewById(R.id.rl_wipedata).setOnClickListener(this);
        findViewById(R.id.rl_aboutapp).setOnClickListener(this);
        findViewById(R.id.rl_login).setOnClickListener(this);

        tv_cache = (TextView) findViewById(R.id.tv_cache);
        tv_login = (TextView) findViewById(R.id.tv_login);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.iv_home:
                this.finish();
                break;
            case R.id.rl_security: {
                startActivity(new Intent(getApplicationContext(), ActivitySecurity.class));
            }
            break;
            case R.id.rl_feedback: {
                Intent intent = new Intent(getApplicationContext(), ActivityFeedback.class);
                startActivity(intent);
            }
            break;
            case R.id.rl_wipedata: {
/*                if (dialogWipeData == null) {
                    dialogWipeData = new Dialog(this);
                    dialogWipeData.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialogWipeData.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    dialogWipeData.setContentView(R.layout.dialog_delete);
                    dialogWipeData.findViewById(R.id.bt_positive).setOnClickListener(this);
                    dialogWipeData.findViewById(R.id.bt_negative).setOnClickListener(this);
                    dialogWipeData.setCancelable(true);
                    dialogWipeData.setCanceledOnTouchOutside(true);
                }
                dialogWipeData.show();*/

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.ActivitySetting_dialog_title);
                builder.setMessage(R.string.ActivitySetting_dialog_message);
                builder.setPositiveButton(R.string.ActivitySetting_dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //只删除下载的文件,清除数据库对应的数据
                        FileUtil.fileDelete(new File(getFilesDir(), "File"));
                        FileUtil.cleanMyDatabase();

                        //主要是清除三个文件夹的内容，cache,files,databases
                        CommonUtil.toast(getString(R.string.ActivitySetting_Toast_清理完毕));
                        tv_cache.setText("0.00B");
                    }
                });
                builder.setNegativeButton(R.string.ActivitySetting_dialog_negative, null);
                builder.show();
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
                    CommonUtil.toast(getString(R.string.ActivitySetting_Toast_登出成功));
                    tv_login.setText(NIMClient.getStatus() == StatusCode.LOGINED ? getString(R.string.ActivitySetting_登出) : getString(R.string.ActivitySetting_登录));
                } else {
                    startActivity(new Intent(this, ActivitySignIn.class));
                }
                break;
        }
    }
}
