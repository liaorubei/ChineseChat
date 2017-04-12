package com.hanwen.chinesechat.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import com.hanwen.chinesechat.R;

import java.math.BigDecimal;

/**
 * 用户协议
 */
public class ActivityUseterm extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_useterm);
        WebView webview = (WebView) findViewById(R.id.webview);
        findViewById(R.id.iv_home).setOnClickListener(this);
        webview.loadUrl("https://www.chinesechat.cn/home/UserServiceAgreement");
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
