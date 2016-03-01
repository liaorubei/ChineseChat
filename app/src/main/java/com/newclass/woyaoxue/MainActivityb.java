package com.newclass.woyaoxue;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.newclass.woyaoxue.fragment.FragmentChoose;
import com.newclass.woyaoxue.fragment.FragmentLineUp;
import com.newclass.woyaoxue.fragment.FragmentPerson;
import com.newclass.woyaoxue.fragment.ListenFragment;
import com.newclass.woyaoxue.service.AutoUpdateService;
import com.newclass.woyaoxue.service.DownloadService;
import com.newclass.woyaoxue.util.ConstantsUtil;
import com.newclass.woyaoxue.util.Log;
import com.voc.woyaoxue.R;

public class MainActivityb extends FragmentActivity implements OnClickListener {
    // Monkey测试代码
    // adb shell monkey -p com.voc.woyaoxue -s 500 --ignore-crashes --ignore-timeouts --monitor-native-crashes -v -v 10000 > E:\log.txt
    //gradlew assemblerelease
    protected static final String TAG = "MainActivity";
    private LinearLayout ll_ctrl;
    private TextView rb_random, rb_listen, rb_person;
    private String release = "student";
    private int currentIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        // 下载任务服务
        Intent sIntent = new Intent(this, DownloadService.class);
        startService(sIntent);
        // 自动升级服务
        Intent service = new Intent(this, AutoUpdateService.class);
        startService(service);


        try {
            release ="teacher";// getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData.getString("release");
            Log.i(TAG, "PackageName:" + getPackageName());
            Log.i(TAG, "onCreate: versionCode=" + getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
            Log.i(TAG, "onCreate: versionName=" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Fragment randomFragment, listenFragment, fragmentPerson;
    private FragmentPagerAdapter kk;

    private void initView() {
        ll_ctrl = (LinearLayout) findViewById(R.id.ll_ctrl);
        rb_random = (TextView) findViewById(R.id.rb_random);
        rb_listen = (TextView) findViewById(R.id.rb_listen);
        rb_person = (TextView) findViewById(R.id.rb_person);

        rb_random.setOnClickListener(this);
        rb_listen.setOnClickListener(this);
        rb_person.setOnClickListener(this);

        // rb_listen.performClick();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rb_random: {
                if (currentIndex == 0) {
                    return;
                }
                currentIndex = 0;
                resetButton();

                if (randomFragment == null) {
                    randomFragment = "student".equals(release) ? new FragmentChoose() : new FragmentLineUp();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, randomFragment).commit();
            }
            break;
            case R.id.rb_listen: {
                if (currentIndex == 1) {
                    return;
                }
                currentIndex = 1;
                resetButton();
                if (listenFragment == null) {
                    listenFragment = new ListenFragment();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, listenFragment).commit();
            }
            break;
            case R.id.rb_person:
                if (currentIndex == 2) {
                    return;
                }
                currentIndex = 2;
                resetButton();
                if (fragmentPerson == null) {
                    fragmentPerson = new FragmentPerson();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, fragmentPerson).commit();
                break;
            default:
                break;
        }
    }

    private void resetButton() {
        rb_random.setTextColor(currentIndex == 0 ? ConstantsUtil.ColorOne : ConstantsUtil.ColorTwo);
        rb_listen.setTextColor(currentIndex == 1 ? ConstantsUtil.ColorOne : ConstantsUtil.ColorTwo);
        rb_person.setTextColor(currentIndex == 2 ? ConstantsUtil.ColorOne : ConstantsUtil.ColorTwo);
    }
}
