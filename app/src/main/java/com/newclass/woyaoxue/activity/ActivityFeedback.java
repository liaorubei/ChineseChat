package com.newclass.woyaoxue.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import java.text.BreakIterator;

public class ActivityFeedback extends Activity implements View.OnClickListener {

    private EditText et_content, et_contact;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        initView();
    }

    private void initView() {
        findViewById(R.id.iv_home).setOnClickListener(this);
        et_content = (EditText) findViewById(R.id.et_content);
        et_contact = (EditText) findViewById(R.id.et_contact);
        findViewById(R.id.bt_positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (progressDialog == null) {
                    progressDialog = new ProgressDialog(ActivityFeedback.this);
                    progressDialog.setMessage("正在提交中。。。");
                }
                progressDialog.show();
                String content = et_content.getText().toString().trim();
                String contact = et_contact.getText().toString().trim();
                if (TextUtils.isEmpty(content)) {
                    CommonUtil.toast("反馈内容不能为空");
                    return;
                }
                HttpUtil.Parameters params = new HttpUtil.Parameters();
                params.add("content", content);
                params.add("contact", contact);
                HttpUtil.post(NetworkUtil.feedbackCreate, params, new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        Response o = new Gson().fromJson(responseInfo.result, new TypeToken<Response>() {
                        }.getType());
                        if (o.code == 200) {
                            CommonUtil.toast("提交成功");
                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        CommonUtil.toast("网络异常");
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_home:
                this.finish();
                break;
        }
    }
}