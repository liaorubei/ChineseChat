package com.newclass.woyaoxue.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;

import com.newclass.woyaoxue.MainActivity;
import com.voc.woyaoxue.R;

public class ActivitySplash extends Activity {

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    };
    private RelativeLayout rl_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        rl_main = (RelativeLayout) findViewById(R.id.rl_main);

        try {
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            String version = sharedPreferences.getString("versionName", "");
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            // sharedPreferences.edit().putString("versionName", versionName).commit();

            if (!TextUtils.equals(version, versionName)) {
                handler.sendEmptyMessageDelayed(1, 2500);
                View child = new View(this);
                child.setBackgroundResource(R.drawable.background);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                rl_main.addView(child, params);
                AnimationSet set = new AnimationSet(true);
                set.addAnimation(new AlphaAnimation(1F, 0F));
                set.addAnimation(new ScaleAnimation(1F, 0F, 1F, 0F, Animation.RELATIVE_TO_SELF, 0.5F, Animation.RELATIVE_TO_SELF, 0.5f));
                set.setDuration(2500);
                set.setFillAfter(true);
                child.startAnimation(set);
            } else {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }


        } catch (Exception ex) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
