package com.newclass.woyaoxue.activity;

import android.os.Bundle;
import android.app.Activity;
import android.text.TextUtils;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.newclass.woyaoxue.ChineseChat;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.bean.User;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

public class ActivitySecurity extends Activity implements View.OnClickListener {

    private static final String TAG = "ActivitySecurity";
    private EditText et_old_password, et_new_password, et_repassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);
        initView();
    }

    private void initView() {
        findViewById(R.id.iv_home).setOnClickListener(this);
        findViewById(R.id.bt_confirm).setOnClickListener(this);

        et_old_password = (EditText) findViewById(R.id.et_old_password);
        et_new_password = (EditText) findViewById(R.id.et_new_password);
        et_repassword = (EditText) findViewById(R.id.et_repassword);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_home:
                this.finish();
                break;
            case R.id.bt_confirm: {
                String old_password = et_old_password.getText().toString().trim();
                String new_password = et_new_password.getText().toString().trim();
                final String repassword = et_repassword.getText().toString().trim();

                if (TextUtils.isEmpty(old_password) || TextUtils.isEmpty(new_password) || TextUtils.isEmpty(repassword)) {
                    CommonUtil.toast(R.string.ActivitySecurity_can_not_null);
                    return;
                }

                if (old_password.length() < 8 || new_password.length() < 8 || repassword.length() < 8) {
                    CommonUtil.toast(R.string.ActivitySecurity_at_least);
                    return;
                }

                if (TextUtils.equals(old_password, new_password)) {
                    CommonUtil.toast(R.string.ActivitySecurity_old_password_new_password_the_same);
                    return;
                }

                if (!TextUtils.equals(new_password, repassword)) {
                    CommonUtil.toast(R.string.ActivitySecurity_new_password_not_same);
                    return;
                }

                HttpUtil.Parameters params = new HttpUtil.Parameters();
                params.add("username", ChineseChat.CurrentUser.Username);
                params.add("old_password", old_password);
                params.add("new_password", new_password);
                HttpUtil.post(NetworkUtil.nimUserModifyPassword, params, new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        Log.i(TAG, "onSuccess: " + responseInfo.result);
                        Response<User> o = new Gson().fromJson(responseInfo.result, new TypeToken<Response<User>>() {
                        }.getType());
                        if (o.code == 200) {
                            CommonUtil.toast(R.string.ActivitySecurity_modify_success);
                            finish();
                        } else {
                            CommonUtil.toast(R.string.ActivitySecurity_modify_failure);
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        Log.i(TAG, "onFailure: " + msg);
                        CommonUtil.toast(R.string.network_error);
                    }
                });
            }
            break;
        }
    }
}
