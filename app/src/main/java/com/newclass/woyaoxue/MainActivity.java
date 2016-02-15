package com.newclass.woyaoxue;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatRingerConfig;
import com.newclass.woyaoxue.activity.TakeActivity;
import com.newclass.woyaoxue.fragment.FragmentChoose;
import com.newclass.woyaoxue.fragment.FragmentLineUp;
import com.newclass.woyaoxue.fragment.FragmentPerson;
import com.newclass.woyaoxue.fragment.ListenFragment;
import com.newclass.woyaoxue.service.AutoUpdateService;
import com.newclass.woyaoxue.service.DownloadService;
import com.newclass.woyaoxue.util.ConstantsUtil;
import com.voc.woyaoxue.R;

public class MainActivity extends FragmentActivity implements OnClickListener {
    // Monkey测试代码
    // adb shell monkey -p com.voc.woyaoxue -s 500 --ignore-crashes --ignore-timeouts --monitor-native-crashes -v -v 10000 > E:\log.txt
    // gradlew assemblerelease
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

        //初始化云信铃声及添加来电观察者(注:不要放在Application里面,有些机子会出现异常)
        enableAVChat();

        // 下载任务服务
        Intent sIntent = new Intent(this, DownloadService.class);
        startService(sIntent);
        // 自动升级服务
        Intent service = new Intent(this, AutoUpdateService.class);
        startService(service);
    }

    private void enableAVChat() {
        setupAVChat();
        registerAVChatIncomingCallObserver(true);
    }

    private void setupAVChat() {
        AVChatRingerConfig config = new AVChatRingerConfig();
        config.res_connecting = R.raw.avchat_connecting;
        config.res_no_response = R.raw.avchat_no_response;
        config.res_peer_busy = R.raw.avchat_peer_busy;
        config.res_peer_reject = R.raw.avchat_peer_reject;
        config.res_ring = R.raw.avchat_ring;
        AVChatManager.getInstance().setRingerConfig(config); // 铃声配置
    }

    private void registerAVChatIncomingCallObserver(boolean register) {

        // 注册来电监听
        AVChatManager.getInstance().observeIncomingCall(new Observer<AVChatData>() {
            @Override
            public void onEvent(AVChatData chatData) {
                TakeActivity.start(MainActivity.this, chatData);
            }
        }, register);
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

        rb_listen.performClick();
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
                    randomFragment = MyApplication.isStudent() ? new FragmentChoose() : new FragmentLineUp();
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
                    listenFragment = new ListenFragment(getSupportFragmentManager());
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
