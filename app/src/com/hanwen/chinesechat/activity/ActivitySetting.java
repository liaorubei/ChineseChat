package com.hanwen.chinesechat.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.TextView;

import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.bean.User;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.FileUtil;
import com.hanwen.chinesechat.R;

import java.io.File;

public class ActivitySetting extends Activity implements View.OnClickListener {
    private static final String TAG = "ActivitySetting";
    private static final int REQUEST_CODE_FINISH = 1;
    private TextView tv_login;
    private AlertDialog dialogLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
        initData();
    }

    @Override
    protected void onResume() {
        //登录菜单的文本显示
        tv_login.setText(NIMClient.getStatus() == StatusCode.LOGINED ? getString(R.string.ActivitySetting_logout) : getString(R.string.ActivitySetting_sginin));
        super.onResume();
    }

    private void initData() {
        //清除缓存菜单数据,只显示两个文件的大小,一个是自己的下载文件夹,一个是数据库文件夹
        long length = FileUtil.fileLength(new File(getFilesDir(), "File")) + FileUtil.fileLength(new File(getFilesDir().getParentFile(), "databases"));
    }

    private void initView() {
        findViewById(R.id.iv_home).setOnClickListener(this);

        View rl_security = findViewById(R.id.rl_security);
        rl_security.setOnClickListener(this);

        findViewById(R.id.rl_feedback).setOnClickListener(this);
        findViewById(R.id.rl_aboutapp).setOnClickListener(this);
        findViewById(R.id.rl_login).setOnClickListener(this);
        tv_login = (TextView) findViewById(R.id.tv_login);
    }

    private void showLoginDialog() {
        if (dialogLogin == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.Fragment_person_dialog_title);
            builder.setMessage(R.string.Fragment_person_dialog_message);
            builder.setPositiveButton(R.string.Fragment_person_dialog_positive, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(ActivitySetting.this, ActivitySignIn.class));
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.Fragment_person_dialog_negative, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            dialogLogin = builder.create();
        }
        dialogLogin.show();
    }

    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.iv_home:
                this.finish();
                break;
            case R.id.rl_security: {
                if (NIMClient.getStatus() != StatusCode.LOGINED) {
                    showLoginDialog();
                    return;
                }


                startActivity(new Intent(getApplicationContext(), ActivitySecurity.class));
            }
            break;
            case R.id.rl_feedback: {
                Intent intent = new Intent(getApplicationContext(), ActivityFeedback.class);
                startActivity(intent);
            }
            break;

            case R.id.rl_aboutapp: {
                Intent intent = new Intent(getApplicationContext(), ActivityAbout.class);
                startActivity(intent);
            }
            break;
            case R.id.rl_login:
                if (NIMClient.getStatus() == StatusCode.LOGINED) {
                    //如果是教师端,教师登出的时候,不管是否在队列中,都直接出队
                    if (!ChineseChat.isStudent()) {
                        HttpUtil.Parameters params = new HttpUtil.Parameters();
                        params.add("Id", ChineseChat.CurrentUser.Id);
                        HttpUtil.post(NetworkUtil.teacherDequeue, params, null);
                    }

                    //登出云信
                    NIMClient.getService(AuthService.class).logout();
                    ChineseChat.CurrentUser = new User();

                    //清空配置
                    getSharedPreferences("user", MODE_PRIVATE).edit().clear().commit();
                    CommonUtil.toast(getString(R.string.ActivitySetting_Toast_logout_success));
                    tv_login.setText(NIMClient.getStatus() == StatusCode.LOGINED ? getString(R.string.ActivitySetting_logout) : getString(R.string.ActivitySetting_sginin));
                } else {
                    Intent intent = new Intent(this, ActivitySignIn.class);
                    startActivityForResult(intent, REQUEST_CODE_FINISH);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult: ");
        if (REQUEST_CODE_FINISH == requestCode && Activity.RESULT_OK == resultCode) {

        }
    }
}
