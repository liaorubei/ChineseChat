package com.newclass.woyaoxue.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.Log;
import com.voc.woyaoxue.R;

public class ActivitySetting extends Activity implements View.OnClickListener {
    private static final String TAG = "SettingActivity";
    private RelativeLayout rl_feedback, rl_login;
    private TextView tv_login;
    private ImageView iv_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
        Log.i(TAG, "onCreate: ");
    }

    private void initView() {
        //标题栏
        iv_home = (ImageView) findViewById(R.id.iv_home);
        findViewById(R.id.rl_feedback).setOnClickListener(this);
        findViewById(R.id.rl_wipedata).setOnClickListener(this);
        findViewById(R.id.rl_aboutapp).setOnClickListener(this);

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
