package com.newclass.woyaoxue.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.newclass.woyaoxue.util.CommonUtil;
import com.voc.woyaoxue.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActivityReset extends Activity implements View.OnClickListener {
    private EditText et_email, et_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);
        initView();
    }

    private void initView() {
        et_email = (EditText) findViewById(R.id.et_email);
        et_code = (EditText) findViewById(R.id.et_code);

        findViewById(R.id.iv_home).setOnClickListener(this);
        findViewById(R.id.tv_code).setOnClickListener(this);
        findViewById(R.id.bt_next).setOnClickListener(this);
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
            }
            break;
            case R.id.bt_next:

                break;
        }
    }
}
