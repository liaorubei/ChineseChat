package com.newclass.woyaoxue.activity;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.EditText;

import com.voc.woyaoxue.R;

public class ActivityFeedback extends Activity {

    private EditText et_content, et_contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        initView();
    }

    private void initView() {
        et_content = (EditText) findViewById(R.id.et_content);
        et_contact = (EditText) findViewById(R.id.et_contact);
        findViewById(R.id.bt_positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
