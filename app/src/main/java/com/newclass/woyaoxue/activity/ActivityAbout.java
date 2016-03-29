package com.newclass.woyaoxue.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.voc.woyaoxue.R;

public class ActivityAbout extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initView();
    }

    private void initView() {
        findViewById(R.id.rl_usehelp).setOnClickListener(this);
        findViewById(R.id.rl_useterm).setOnClickListener(this);
        findViewById(R.id.iv_home).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_home:
                this.finish();
                break;
            case R.id.rl_usehelp: {
                Intent intent = new Intent(getApplicationContext(), ActivityUsehelp.class);
                startActivity(intent);
            }
            break;
            case R.id.rl_useterm: {
                Intent intent = new Intent(getApplicationContext(), ActivityUseterm.class);
                startActivity(intent);
            }
            break;
        }
    }
}
