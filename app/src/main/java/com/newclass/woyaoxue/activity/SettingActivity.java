package com.newclass.woyaoxue.activity;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;

import android.view.View;
import android.widget.Button;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.AuthService;
import com.newclass.woyaoxue.util.Log;
import com.voc.woyaoxue.R;

public class SettingActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "SettingActivity";
    private Button bt_login, bt_logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
        Log.i(TAG, "onCreate: ");
    }

    private void initView() {
        bt_login = (Button) findViewById(R.id.bt_login);
        bt_logout = (Button) findViewById(R.id.bt_logout);

        bt_login.setOnClickListener(this);
        bt_logout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_login:
                startActivity(new Intent(this, SignInActivity.class));
                break;
            case R.id.bt_logout:
                NIMClient.getService(AuthService.class).logout();
                getSharedPreferences("user", MODE_PRIVATE).edit().clear().commit();
                break;
        }
    }
}
