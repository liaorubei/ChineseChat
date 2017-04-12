package com.hanwen.chinesechat.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.util.NetworkUtil;

public class ActivityUsehelp extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usehelp);
        WebView webview = (WebView) findViewById(R.id.webview);
        webview.loadUrl(String.format("%1$s%2$s", NetworkUtil.domain, "/Home/UserHelp"));
        findViewById(R.id.iv_home).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_home:
                finish();
                break;
        }
    }
}
