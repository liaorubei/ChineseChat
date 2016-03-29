package com.newclass.woyaoxue.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import com.voc.woyaoxue.R;

import java.math.BigDecimal;

/**
 * Created by 儒北 on 2016/3/23.
 */
public class ActivityUseterm extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_useterm);
        WebView webview = (WebView) findViewById(R.id.webview);
        findViewById(R.id.iv_home).setOnClickListener(this);
        webview.loadUrl("http://voc2015.azurewebsites.net/home/UserServiceAgreement");
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
