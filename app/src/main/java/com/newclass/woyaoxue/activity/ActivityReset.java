package com.newclass.woyaoxue.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Size;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.newclass.woyaoxue.MyApplication;
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
                        tv.setText(MyApplication.getContext().getString(R.string.ActivityReset_get_code));
                        tv.setEnabled(true);
                    }
                    break;
            }
        }
    };
    private static int time = 60;
    private Gson gson = new Gson();
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);
        initView();

        toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
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
                    toast.setText(R.string.ActivityReset_please_input_email);
                    toast.show();
                    return;
                }

                //验证邮箱格式
                String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
                Pattern regex = Pattern.compile(check);
                Matcher matcher = regex.matcher(email);
                boolean isMatched = matcher.matches();
                if (!isMatched) {
                    toast.setText(R.string.ActivityReset_EmailAddressFormatIsNotCorrect);
                    toast.show();
                    return;
                }

                toast.setText(R.string.ActivityReset_Please_check_your_email);
                toast.show();

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
                        toast.setText(R.string.ActivityReset_get_code_error);
                        toast.show();
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
                    toast.setText(R.string.ActivityReset_email_and_code_can_not_be_null);
                    toast.show();
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
                        CommonUtil.toast(getString(R.string.network_error));
                        Log.i(TAG, "onFailure: " + msg);
                    }
                });

            }
            break;
            case R.id.bt_positive: {
                String email = et_email.getText().toString().trim();
                String password = et_password.getText().toString().trim();
                String re_password = et_repassword.getText().toString().trim();
                if (TextUtils.isEmpty(re_password) || TextUtils.isEmpty(re_password)) {
                    toast.setText(R.string.ActivityReset_please_input_email);
                    toast.show();
                    return;
                }

                if (!TextUtils.equals(password, re_password)) {
                    toast.setText(R.string.ActivityReset_password_are_not_the_same);
                    toast.show();
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
                            toast.setText(R.string.ActivityReset_password_change_success);
                            toast.show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        CommonUtil.toast(getString(R.string.network_error));
                        Log.i(TAG, "onFailure: " + msg);
                    }
                });
            }
            break;
        }
    }
}
