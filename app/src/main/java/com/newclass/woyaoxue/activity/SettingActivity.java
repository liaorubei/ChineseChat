package com.newclass.woyaoxue.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.newclass.woyaoxue.util.Log;
import com.voc.woyaoxue.R;

public class SettingActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "SettingActivity";
    private Button bt_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
        Log.i(TAG, "onCreate: ");
    }

    private void initView() {
        bt_login = (Button) findViewById(R.id.bt_login);

        bt_login.setText(NIMClient.getStatus() == StatusCode.LOGINED ? "登出" : "登录");
        bt_login.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.bt_login:
                if (NIMClient.getStatus() == StatusCode.LOGINED) {
                    NIMClient.getService(AuthService.class).logout();
                    getSharedPreferences("user", MODE_PRIVATE).edit().clear().commit();
                } else {
                    startActivity(new Intent(this, ActivitySignIn.class));
                }
                break;
        }
    }
}
