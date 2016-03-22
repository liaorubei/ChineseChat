package com.newclass.woyaoxue.activity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.bean.User;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.HttpUtil.Parameters;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 帐号注册界面
 *
 * @author liaorubei
 */
public class ActivitySignUp extends Activity implements OnClickListener {
    public static final int SignUp = 0;
    private EditText et_email, et_password, et_repassword;
    private View tv_protocol, tv_login;
    private CheckBox cb_is_read;
    private Button bt_signup;

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

        cb_is_read = (CheckBox) findViewById(R.id.cb_is_read);
        cb_is_read.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                bt_signup.setEnabled(isChecked);
            }
        });

        findViewById(R.id.tv_protocol).setOnClickListener(this);
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
            case R.id.tv_protocol:
                CommonUtil.toast("暂时无");
                break;
            case R.id.bt_signup: {
                String email = et_email.getText().toString().trim();
                String password = et_password.getText().toString().trim();
                String repassword = et_repassword.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(repassword)) {
                    CommonUtil.toast("数据不能为空");
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

                if (!password.equals(repassword)) {
                    CommonUtil.toast("两次输入的密码不一致");
                    return;
                }

                Parameters parameters = new Parameters();
                parameters.add("email", email);
                parameters.add("password", password);
                parameters.add("category", "" + 0);
                HttpUtil.post(NetworkUtil.userCreate, parameters, new RequestCallBack<String>() {

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        bt_signup.setEnabled(true);
                        Response<User> json = new Gson().fromJson(responseInfo.result, new TypeToken<Response<User>>() {
                        }.getType());

                        Log.i("logi", "创建成功:" + json.toString());
                        if (200 == json.code) {
                            Toast.makeText(ActivitySignUp.this, "注册成功", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(ActivitySignUp.this, "网络异常,请重试", Toast.LENGTH_SHORT).show();
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
