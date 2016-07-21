package com.hanwen.chinesechat.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.*;
import android.os.Process;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.util.Log;

public class TestActivity extends FragmentActivity {
    private static final String TAG = "TestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        findViewById(R.id.btn_wx).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: RECORD_AUDIO GRANTED=" + (checkPermission(Manifest.permission.RECORD_AUDIO, android.os.Process.myPid(), Process.myUid()) == PackageManager.PERMISSION_GRANTED));
                grantUriPermission(null,null,0);

            }
        });


    }

}
