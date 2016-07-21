package com.hanwen.chinesechat.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.util.Log;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.bean.User;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.HttpUtil.Parameters;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 帐号注册界面
 *
 * @author liaorubei
 */
public class ActivitySignUp extends Activity implements OnClickListener {
    public static final int SignUp = 0;
    private static final String TAG = "ActivitySignUp";
    private EditText et_email, et_password, et_repassword;
    private View tv_protocol, tv_login;
    private Button bt_signup;
    private View tv_read;
    private View iv_read;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        initView();
    }

    private void initView() {
        findViewById(R.id.iv_home).setOnClickListener(this);

        et_email = (EditText) findViewById(R.id.et_email);
        et_password = (EditText) findViewById(R.id.et_password);
        et_repassword = (EditText) findViewById(R.id.et_repassword);

        iv_read = findViewById(R.id.iv_read);
        iv_read.setOnClickListener(this);
        tv_read = findViewById(R.id.tv_read);
        tv_read.setOnClickListener(this);

        TextView tv_protocol = (TextView) findViewById(R.id.tv_protocol);
        tv_protocol.setPaintFlags(tv_protocol.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tv_protocol.setOnClickListener(this);
        findViewById(R.id.tv_login).setOnClickListener(this);

        bt_signup = (Button) findViewById(R.id.bt_signup);
        bt_signup.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_home:
                this.finish();
                break;
            case R.id.iv_read:
            case R.id.tv_read:
                iv_read.setSelected(!iv_read.isSelected());
                bt_signup.setEnabled(iv_read.isSelected());
                break;
            case R.id.tv_protocol:
                startActivity(new Intent(this, ActivityUseterm.class));
                break;
            case R.id.bt_signup: {
                String email = et_email.getText().toString().trim();
                String password = et_password.getText().toString().trim();
                String repassword = et_repassword.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(repassword)) {
                    CommonUtil.toast(R.string.ActivitySignUp_email_password_can_not_null);
                    return;
                }

                //验证邮箱格式
                String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
                Pattern regex = Pattern.compile(check);
                Matcher matcher = regex.matcher(email);
                boolean isMatched = matcher.matches();
                if (!isMatched) {
                    CommonUtil.toast(R.string.ActivitySignUp_email_format_wrong);
                    return;
                }

                if (!password.equals(repassword)) {
                    CommonUtil.toast(R.string.ActivitySignUp_password_are_not_same);
                    return;
                }

                //取得手机设备号和手机型号
                String deviceId = "";
                String deviceType = "";
                try {
                    deviceId = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
                    deviceType = Build.MODEL;
                } catch (Exception ex) {
                    Log.i(TAG, "onClick: " + ex.getMessage());
                }

                //要求区分学生帐号还是教师帐号,要求
                Parameters parameters = new Parameters();
                parameters.add("email", email);
                parameters.add("password", password);
                parameters.add("category", ChineseChat.isStudent() ? 0 : 1);
                parameters.add("deviceId", deviceId);
                parameters.add("deviceType", deviceType);
                HttpUtil.post(NetworkUtil.userCreate, parameters, new RequestCallBack<String>() {

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        bt_signup.setEnabled(true);
                        Response<User> json = new Gson().fromJson(responseInfo.result, new TypeToken<Response<User>>() {
                        }.getType());

                        if (200 == json.code) {
                            CommonUtil.toast(R.string.ActivitySignUp_register_success);
                            // 返回注册信息
                            Intent data = new Intent();
                            data.putExtra("email", et_email.getText().toString().trim());
                            data.putExtra("password", et_password.getText().toString().trim());
                            setResult(SignUp, data);
                            finish();
                        } else {
                            Toast.makeText(ActivitySignUp.this, json.desc, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        CommonUtil.toast(R.string.network_error);
                        bt_signup.setEnabled(true);
                    }
                });
                bt_signup.setEnabled(false);
            }
            break;
            case R.id.tv_login:
                this.finish();
                break;
            default:
                break;
        }
    }
}
