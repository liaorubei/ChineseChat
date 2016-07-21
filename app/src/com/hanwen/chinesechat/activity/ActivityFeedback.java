package com.hanwen.chinesechat.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.ChineseChat;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.R;

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
        et_contact.setText(ChineseChat.CurrentUser.Email);

        findViewById(R.id.bt_positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (progressDialog == null) {
                    progressDialog = new ProgressDialog(ActivityFeedback.this);
                    progressDialog.setMessage(getString(R.string.ActivityFeedback_Content_IsBeingSubmitted));
                }
                progressDialog.show();
                String content = et_content.getText().toString().trim();
                String contact = et_contact.getText().toString().trim();
                if (TextUtils.isEmpty(content)) {
                    CommonUtil.toast(getString(R.string.ActivityFeedback_Content_CanNotNull));
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
                            CommonUtil.toast(getString(R.string.ActivityFeedback_Content_submit_success));
                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        CommonUtil.toast(getString(R.string.network_error));
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
