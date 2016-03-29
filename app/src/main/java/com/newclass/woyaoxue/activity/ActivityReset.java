package com.newclass.woyaoxue.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.db.sqlite.CursorUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.bean.User;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActivityReset extends Activity implements View.OnClickListener {
    private static final String TAG = "ActivityReset";
    private static final int CHANGE_TIME_TEXT = 1;
    private EditText et_email, et_code, et_password, et_repassword;
    private TextView tv_code;
    private LinearLayout ll_code, ll_reset;

    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CHANGE_TIME_TEXT:
                    time--;
                    TextView tv = (TextView) msg.obj;
                    Log.i(TAG, "handleMessage: " + time);
                    if (time > 0) {
                        tv.setText(time + "S");
                        Message message = handler.obtainMessage();
                        message.what = CHANGE_TIME_TEXT;
                        message.obj = tv;
                        sendMessageDelayed(message, 1000);
                    } else {
                        tv.setText("获取验证码");
                        tv.setEnabled(true);
                    }
                    break;
            }
        }
    };
    private static int time = 60;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);
        initView();
    }

    private void initView() {
        et_email = (EditText) findViewById(R.id.et_email);
        et_code = (EditText) findViewById(R.id.et_code);
        tv_code = (TextView) findViewById(R.id.tv_code);
        findViewById(R.id.bt_next).setOnClickListener(this);
        tv_code.setOnClickListener(this);

        et_password = (EditText) findViewById(R.id.et_password);
        et_repassword = (EditText) findViewById(R.id.et_repassword);
        findViewById(R.id.bt_positive).setOnClickListener(this);

        ll_code = (LinearLayout) findViewById(R.id.ll_code);
        ll_reset = (LinearLayout) findViewById(R.id.ll_reset);
        findViewById(R.id.iv_home).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);//清除所有的回调和消息
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_home:
                this.finish();
                break;
            case R.id.tv_code: {
                String email = et_email.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    CommonUtil.toast("请输入邮箱");
                    return;
                }

                //验证邮箱格式
                String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
                Pattern regex = Pattern.compile(check);
                Matcher matcher = regex.matcher(email);
                boolean isMatched = matcher.matches();
                if (!isMatched) {
                    CommonUtil.toast("邮箱格式不正确");
                    return;
                }

                CommonUtil.toast("请到邮箱获取验证码");
                time = 60;
                tv_code.setText("60S");
                tv_code.setEnabled(false);
                Message message = handler.obtainMessage();
                message.what = CHANGE_TIME_TEXT;
                message.obj = tv_code;
                handler.sendMessageDelayed(message, 1000);

                HttpUtil.Parameters params = new HttpUtil.Parameters();
                params.add("email", email);
                HttpUtil.post(NetworkUtil.nimuserGetCode, params, new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        Log.i(TAG, "onSuccess: " + responseInfo.result);
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        CommonUtil.toast("获取验证码失败");
                        tv_code.setEnabled(true);
                        Log.i(TAG, "onFailure: " + msg);
                    }
                });


            }
            break;
            case R.id.bt_next: {
                String email = et_email.getText().toString().trim();
                String code = et_code.getText().toString().trim();
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(code)) {
                    CommonUtil.toast("邮箱或验证码不能为空");
                    return;
                }

                HttpUtil.Parameters params = new HttpUtil.Parameters();
                params.add("contact", email);
                params.add("captcha", code);
                HttpUtil.post(NetworkUtil.nimuserVerify, params, new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        Response<String> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<String>>() {
                        }.getType());

                        if (resp.code == 200) {
                            ll_code.setVisibility(View.INVISIBLE);
                            ll_reset.setVisibility(View.VISIBLE);
                        } else {
                            CommonUtil.toast(resp.desc);
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        CommonUtil.toast("网络异常");
                        Log.i(TAG, "onFailure: " + msg);
                    }
                });

            }
            break;
            case R.id.bt_positive: {
                String email = et_email.getText().toString().trim();
                String password = et_password.getText().toString().trim();
                String repassword = et_repassword.getText().toString().trim();
                if (TextUtils.isEmpty(repassword) || TextUtils.isEmpty(repassword)) {
                    CommonUtil.toast("请输入密码");
                    return;
                }

                if (!TextUtils.equals(password, repassword)) {
                    CommonUtil.toast("两次输入的密码不一样");
                    return;
                }

                HttpUtil.Parameters params = new HttpUtil.Parameters();
                params.add("email", email);
                params.add("password", password);
                HttpUtil.post(NetworkUtil.nimuserChangePassword, params, new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        Response<User> user = gson.fromJson(responseInfo.result, new TypeToken<Response<User>>() {
                        }.getType());
                        if (user.code == 200) {
                            CommonUtil.toast("密码修改成功，请使用新密码登录");
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        CommonUtil.toast("网络异常");
                        Log.i(TAG, "onFailure: " + msg);
                    }
                });
            }
            break;
        }
    }
}
